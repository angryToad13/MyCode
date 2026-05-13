// validation.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { FormGroup } from '@angular/forms';
import { Observable, from, of } from 'rxjs';
import {
  concatMap,
  filter,
  tap,
  map,
  catchError
} from 'rxjs/operators';

export interface ValidationResponse {
  valid: boolean;
  message?: string;
}

@Injectable({
  providedIn: 'root'
})
export class ValidationService {

  constructor(
    private http: HttpClient
  ) {}

  runValidations(
    validationString: string,
    formGroup: FormGroup
  ): Observable<void> {

    const validations = validationString
      ?.split(',')
      .map(v => v.trim())
      .filter(Boolean);

    return from(validations).pipe(
      concatMap(validationName =>
        this.executeValidation(validationName, formGroup).pipe(
          filter(response => response.valid),
          tap(response => {
            this.showPrimeNgPopup(
              `${validationName} success`,
              response.message || 'Validation passed'
            );
          }),
          map(() => void 0)
        )
      )
    );
  }

  private executeValidation(
    validationName: string,
    formGroup: FormGroup
  ): Observable<ValidationResponse> {

    const validationMap: Record<
      string,
      () => Observable<ValidationResponse>
    > = {
      'document-validation': () =>
        this.documentValidation(formGroup),

      'currency-validation': () =>
        this.currencyValidation(formGroup)
    };

    const validationMethod = validationMap[validationName];

    if (!validationMethod) {
      console.warn(`No validation method found for ${validationName}`);
      return of({ valid: false });
    }

    return validationMethod().pipe(
      catchError(() => of({ valid: false }))
    );
  }

  // -----------------------------------
  // Validation methods with form values
  // -----------------------------------

  private documentValidation(
    formGroup: FormGroup
  ): Observable<ValidationResponse> {

    const payload = {
      documentNumber: formGroup.get('documentNumber')?.value,
      customerName: formGroup.get('customerName')?.value
    };

    return this.http.post<ValidationResponse>(
      '/api/document-validation',
      payload
    );
  }

  private currencyValidation(
    formGroup: FormGroup
  ): Observable<ValidationResponse> {

    const payload = {
      currency: formGroup.get('currency')?.value,
      amount: formGroup.get('amount')?.value
    };

    return this.http.post<ValidationResponse>(
      '/api/currency-validation',
      payload
    );
  }

  private showPrimeNgPopup(
    summary: string,
    detail: string
  ): void {

    // this.messageService.add({
    //   severity: 'success',
    //   summary,
    //   detail
    // });

    alert(`${summary} - ${detail}`);
  }
}