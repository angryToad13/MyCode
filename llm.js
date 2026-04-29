import { Injectable } from '@angular/core';
import { Resolve, ActivatedRouteSnapshot, Router } from '@angular/router';
import { Observable, EMPTY } from 'rxjs';
import { switchMap, map, catchError } from 'rxjs/operators';
import { LlmNavigationServiceService } from './llm-navigation-service.service';
import { LlmDataServiceService } from './llm-data-service.service';

type ResolverConfig = {
  getNavData: () => Observable<any>;
  api: (eventId: string, documentId: string, documentVersion: number, data: any) => Observable<any>;
  redirect: string;
};

@Injectable({ providedIn: 'root' })
export class LlmDataResolver implements Resolve<any> {

  constructor(
    private llmNavigationService: LlmNavigationServiceService,
    private llmDataService: LlmDataServiceService,
    private router: Router
  ) {}

  resolve(route: ActivatedRouteSnapshot): Observable<any> {

    const eventId = route.paramMap.get('requestOrEventId') ?? '';
    const documentId = route.paramMap.get('documentId') ?? '';
    const documentVersion = Number(route.paramMap.get('documentVersion')) ?? 1;

    const config: Record<'extraction' | 'classification' | 'evaluation', ResolverConfig> = {
      extraction: {
        getNavData: () => this.llmNavigationService.getExtractionData(),
        api: this.llmDataService.getDataEvaluation.bind(this.llmDataService),
        redirect: '/data-extraction'
      },
      classification: {
        getNavData: () => this.llmNavigationService.getClassificationData(),
        api: this.llmDataService.getDataExtraction.bind(this.llmDataService),
        redirect: '/data-classification'
      },
      evaluation: {
        getNavData: () => this.llmNavigationService.getEvaluationData(),
        api: this.llmDataService.getDataExtraction.bind(this.llmDataService),
        redirect: '/data-extraction'
      }
    };

    const type = route.data['type'] as 'extraction' | 'classification' | 'evaluation';
    const selected = config[type];

    return selected.getNavData().pipe(
      switchMap((data: any) => {
        if (!data || Object.keys(data).length === 0) {
          this.router.navigate([selected.redirect]);
          return EMPTY;
        }
        return selected.api(eventId, documentId, documentVersion, data);
      }),
      map((res: any) => res.results ?? res.result),
      catchError(() => {
        this.router.navigate([selected.redirect]);
        return EMPTY;
      })
    );
  }
}