// validation.service.ts
import { Injectable } from '@angular/core';
import { FormGroup } from '@angular/forms';

import {
  DialogService,
  DynamicDialogRef
} from 'primeng/dynamicdialog';

import {
  Observable,
  of,
  from,
  throwError
} from 'rxjs';

import {
  concatMap,
  switchMap
} from 'rxjs/operators';

import { ValidationDialogComponent } from './validation-dialog.component';

export interface ValidationResult {
  shouldShowPopup: boolean;
  message?: string;
}

@Injectable({
  providedIn: 'root'
})
export class ValidationService {

  constructor(
    private dialogService: DialogService
  ) {}

  runValidations(
    validationString: string,
    form: FormGroup
  ): Observable<boolean> {

    const validations = validationString
      ?.split(',')
      .map(v => v.trim())
      .filter(Boolean);

    return from(validations).pipe(

      concatMap(validationName =>

        this.executeValidation(validationName, form).pipe(

          switchMap(result => {

            if (!result.shouldShowPopup) {
              return of(true);
            }

            return this.openValidationDialog(
              result.message || 'Continue?'
            );
          })
        )
      )
    );
  }

  private executeValidation(
    validationName: string,
    form: FormGroup
  ): Observable<ValidationResult> {

    const validationMap: Record<
      string,
      () => Observable<ValidationResult>
    > = {

      'document-validation': () =>
        this.documentValidation(form),

      'currency-validation': () =>
        this.currencyValidation(form)
    };

    return validationMap[validationName]
      ? validationMap[validationName]()
      : of({
          shouldShowPopup: false
        });
  }

  // -------------------------------------
  // VALIDATIONS
  // -------------------------------------

  private documentValidation(
    form: FormGroup
  ): Observable<ValidationResult> {

    const hasIssue = true;

    return of({
      shouldShowPopup: hasIssue,
      message: 'Document validation failed. Continue?'
    });
  }

  private currencyValidation(
    form: FormGroup
  ): Observable<ValidationResult> {

    const hasIssue = true;

    return of({
      shouldShowPopup: hasIssue,
      message: 'Currency mismatch found. Continue?'
    });
  }

  // -------------------------------------
  // DYNAMIC DIALOG
  // -------------------------------------

  private openValidationDialog(
    message: string
  ): Observable<boolean> {

    return new Observable<boolean>((observer) => {

      const ref: DynamicDialogRef =
        this.dialogService.open(
          ValidationDialogComponent,
          {
            header: 'Confirmation',
            width: '450px',
            closable: false,
            dismissableMask: false,
            data: {
              message
            }
          }
        );

      ref.onClose.subscribe((accepted: boolean) => {

        if (accepted) {
          observer.next(true);
          observer.complete();
        } else {
          observer.error(false);
        }
      });

      ref.onDestroy.subscribe(() => {
        ref.close();
      });
    });
  }
}