// validation.service.ts
import { Injectable } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { ConfirmationService } from 'primeng/api';

import {
  Observable,
  from,
  of,
  throwError
} from 'rxjs';

import {
  concatMap,
  switchMap
} from 'rxjs/operators';

export interface ValidationResult {
  shouldShowPopup: boolean;
  message?: string;
}

@Injectable({
  providedIn: 'root'
})
export class ValidationService {

  constructor(
    private confirmationService: ConfirmationService
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

      // execute ONE by ONE
      concatMap(validationName =>

        this.executeValidation(validationName, form).pipe(

          // popup only if needed
          switchMap(result => {

            if (!result.shouldShowPopup) {
              return of(true);
            }

            // wait until popup accepted
            return this.showConfirmationPopup(
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

  // ----------------------------------
  // VALIDATIONS
  // ----------------------------------

  private documentValidation(
    form: FormGroup
  ): Observable<ValidationResult> {

    // API call here

    const hasIssue = true;

    return of({
      shouldShowPopup: hasIssue,
      message: 'Document validation failed. Continue?'
    });
  }

  private currencyValidation(
    form: FormGroup
  ): Observable<ValidationResult> {

    // API call here

    const hasIssue = true;

    return of({
      shouldShowPopup: hasIssue,
      message: 'Currency mismatch found. Continue?'
    });
  }

  // ----------------------------------
  // SINGLE REUSABLE POPUP
  // ----------------------------------

  private showConfirmationPopup(
    message: string
  ): Observable<boolean> {

    return new Observable<boolean>((observer) => {

      this.confirmationService.confirm({
        key: 'validation-popup',
        header: 'Confirmation',
        message,

        accept: () => {
          observer.next(true);
          observer.complete();
        },

        reject: () => {
          observer.error('User rejected');
        }
      });
    });
  }
}