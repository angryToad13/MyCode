// validation-dialog.service.ts
import { Injectable } from '@angular/core';
import {
  BehaviorSubject,
  Observable,
  Subject
} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ValidationDialogService {

  private messageSubject =
    new BehaviorSubject<string>('');

  message$ =
    this.messageSubject.asObservable();

  private responseSubject =
    new Subject<boolean>();

  response$ =
    this.responseSubject.asObservable();

  private visibleSubject =
    new BehaviorSubject<boolean>(false);

  visible$ =
    this.visibleSubject.asObservable();

  open(message: string): Observable<boolean> {

    this.messageSubject.next(message);

    this.visibleSubject.next(true);

    return this.response$;
  }

  accept(): void {
    this.responseSubject.next(true);
    this.visibleSubject.next(false);
  }

  reject(): void {
    this.responseSubject.next(false);
    this.visibleSubject.next(false);
  }
}


// validation-dialog.component.ts
import { Component } from '@angular/core';
import { ValidationDialogService } from './validation-dialog.service';

@Component({
  selector: 'app-validation-dialog',
  template: `
    <p-dialog
      header="Confirmation"
      [modal]="true"
      [closable]="false"
      [dismissableMask]="false"
      [(visible)]="visible">

      <p class="mb-4">
        {{ message$ | async }}
      </p>

      <div class="flex justify-content-end gap-2">

        <button
          pButton
          type="button"
          label="Cancel"
          class="p-button-text"
          (click)="reject()">
        </button>

        <button
          pButton
          type="button"
          label="Continue"
          (click)="accept()">
        </button>

      </div>
    </p-dialog>
  `
})
export class ValidationDialogComponent {

  message$ =
    this.validationDialogService.message$;

  visible = false;

  constructor(
    private validationDialogService: ValidationDialogService
  ) {

    this.validationDialogService.visible$
      .subscribe(v => {
        this.visible = v;
      });
  }

  accept(): void {
    this.validationDialogService.accept();
  }

  reject(): void {
    this.validationDialogService.reject();
  }
}


// validation.service.ts
import { Injectable } from '@angular/core';
import { FormGroup } from '@angular/forms';

import {
  Observable,
  from,
  of,
  throwError
} from 'rxjs';

import {
  concatMap,
  switchMap,
  take
} from 'rxjs/operators';

import {
  ValidationDialogService
} from './validation-dialog.service';

export interface ValidationResult {
  shouldShowPopup: boolean;
  message?: string;
}

@Injectable({
  providedIn: 'root'
})
export class ValidationService {

  constructor(
    private validationDialogService:
      ValidationDialogService
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

        this.executeValidation(
          validationName,
          form
        ).pipe(

          switchMap(result => {

            if (!result.shouldShowPopup) {
              return of(true);
            }

            return this.validationDialogService
              .open(result.message || 'Continue?')
              .pipe(

                take(1),

                switchMap(accepted => {

                  if (accepted) {
                    return of(true);
                  }

                  return throwError(() =>
                    new Error('Rejected')
                  );
                })
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

  private documentValidation(
    form: FormGroup
  ): Observable<ValidationResult> {

    return of({
      shouldShowPopup: true,
      message: 'Document validation failed'
    });
  }

  private currencyValidation(
    form: FormGroup
  ): Observable<ValidationResult> {

    return of({
      shouldShowPopup: true,
      message: 'Currency mismatch found'
    });
  }
}

