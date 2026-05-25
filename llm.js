
interface GenericFlowOverrides<TMapping, TResult> {

  shouldExecute?: () => boolean;

  onValidationFail?: () => void;

  afterMapping?: (mapping: TMapping[]) => void;

  onError?: (error: Error) => Observable<any>;
}


private executeGenericFlow<TMapping, TResult>(
  baseConfig: {
    fetchMapping: () => Observable<TMapping[]>;
    execute: (mapping: TMapping[]) => Observable<TResult>;
  },
  overrides?: GenericFlowOverrides<TMapping, TResult>
): void {

  // default behaviour
  const defaultConfig = {

    shouldExecute: () => true,

    onValidationFail: () => {
      console.warn('Validation failed');
    },

    afterMapping: (_mapping: TMapping[]) => {},

    onError: (error: Error) => {
      console.error(error);
      return of(error);
    }
  };

  // merge defaults with overrides
  const config = {
    ...defaultConfig,
    ...overrides
  };

  if (!config.shouldExecute()) {
    config.onValidationFail();
    return;
  }

  baseConfig.fetchMapping()
    .pipe(

      tap((mapping) => {
        config.afterMapping(mapping);
      }),

      switchMap((mapping) => {

        if (!mapping?.length) {
          return of({
            response: 'Invalid mapping data'
          } as TResult);
        }

        return baseConfig.execute(mapping);
      }),

      catchError((error: Error) => {
        return config.onError(error);
      })

    )
    .subscribe();
}



triggerXmlGeneration(
  request: Request,
  action: string,
  mapperFunction: EXTERNAL_TNT_MAPPER = EXTERNAL_TNT_MAPPER.DEFAULT_MAP,
  overrides?: GenericFlowOverrides<
    ExternalTrackAndTraceMapping,
    StringResponse
  >
): void {

  const ebCusFlag = get(request, 'ebCusflag', null);
  const cxtStatus = get(request, 'cxtTrackAndTraceStatus', null);
  const isUpdate = get(request, 'event.isUpdate', 'No');

  const rsql = generateRsql<RequestExtended>(
    externalTrackAndTraceRsqlFields,
    { ...request, action }
  );

  this.executeGenericFlow({

    fetchMapping: () =>
      this.initiatEventDataService
        .getExternalTrackAndTraceMapping(rsql),

    execute: (mappingData) =>
      this.initiatEventDataService
        .sendDataToConnexis(
          this.actionMap[mapperFunction]?.(
            request,
            mappingData[0]
          )
        )

  }, {

    // DEFAULT CONDITIONS
    shouldExecute: () =>
      VALID_CXT_STATUS.includes(cxtStatus) &&
      isUpdate === IS_UPDATE_FLAG &&
      ebCusFlag === ALLOWED_EB_CUS,

    ...overrides
  });
}
