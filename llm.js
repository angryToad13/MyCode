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

  readonly message$ =
    this.messageSubject.asObservable();

  private visibleSubject =
    new BehaviorSubject<boolean>(false);

  readonly visible$ =
    this.visibleSubject.asObservable();

  private responseSubject =
    new Subject<boolean>();

  readonly response$ =
    this.responseSubject.asObservable();

  open(
    message: string,
    isLastValidation: boolean
  ): Observable<boolean> {

    this.messageSubject.next(message);

    // open only once
    if (!this.visibleSubject.value) {
      this.visibleSubject.next(true);
    }

    // store last validation state
    this.isLastValidation = isLastValidation;

    return this.response$;
  }

  private isLastValidation = false;

  accept(): void {

    this.responseSubject.next(true);

    // close ONLY after last validation
    if (this.isLastValidation) {
      this.visibleSubject.next(false);
    }
  }

  reject(): void {

    this.responseSubject.next(false);

    // reject should always close
    this.visibleSubject.next(false);
  }
}





runValidations(
  validationString: string,
  form: FormGroup
): Observable<boolean> {

  const validations = validationString
    ?.split(',')
    .map(v => v.trim())
    .filter(Boolean);

  return from(validations).pipe(

    concatMap((validationName, index) =>

      this.executeValidation(
        validationName,
        form
      ).pipe(

        switchMap(result => {

          if (!result.shouldShowPopup) {
            return of(true);
          }

          const isLastValidation =
            index === validations.length - 1;

          return this.validationDialogService
            .open(
              result.message || 'Continue?',
              isLastValidation
            )
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