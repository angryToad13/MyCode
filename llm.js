// validation.service.ts
import { Injectable } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { ConfirmationService } from 'primeng/api';
import {
  Observable,
  from,
  of
} from 'rxjs';

import {
  concatMap,
  switchMap
} from 'rxjs/operators';

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

      // executes sequentially
      concatMap(validationName =>
        this.executeValidation(validationName, form)
      ),

      // stop execution immediately on reject
      switchMap((isValid: boolean) => {
        if (!isValid) {
          throw new Error('Validation rejected');
        }

        return of(true);
      })
    );
  }

  private executeValidation(
    validationName: string,
    form: FormGroup
  ): Observable<boolean> {

    const validationMap: Record<
      string,
      () => Observable<boolean>
    > = {

      'document-validation': () =>
        this.documentValidation(form),

      'currency-validation': () =>
        this.currencyValidation(form)
    };

    return validationMap[validationName]
      ? validationMap[validationName]()
      : of(true);
  }

  // ------------------------------------------------
  // DOCUMENT VALIDATION
  // ------------------------------------------------

  private documentValidation(
    form: FormGroup
  ): Observable<boolean> {

    const hasIssue = true;

    if (!hasIssue) {
      return of(true);
    }

    return new Observable<boolean>((observer) => {

      this.confirmationService.confirm({
        key: 'document-warning',
        header: 'Confirmation',
        message: 'Document validation failed. Continue?',

        accept: () => {
          observer.next(true);
          observer.complete();
        },

        reject: () => {
          observer.next(false);
          observer.complete();
        }
      });
    });
  }

  // ------------------------------------------------
  // CURRENCY VALIDATION
  // ------------------------------------------------

  private currencyValidation(
    form: FormGroup
  ): Observable<boolean> {

    const hasIssue = true;

    if (!hasIssue) {
      return of(true);
    }

    return new Observable<boolean>((observer) => {

      this.confirmationService.confirm({
        key: 'currency-warning',
        header: 'Confirmation',
        message: 'Currency mismatch found. Continue?',

        accept: () => {
          observer.next(true);
          observer.complete();
        },

        reject: () => {
          observer.next(false);
          observer.complete();
        }
      });
    });
  }
}