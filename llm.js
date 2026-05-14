// validation-dialog.component.ts
import { Component } from '@angular/core';
import { DynamicDialogRef } from 'primeng/dynamicdialog';

@Component({
  selector: 'app-validation-dialog',
  template: `
    <div class="p-4">
      <p class="mb-4 text-lg">
        {{ message }}
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
    </div>
  `
})
export class ValidationDialogComponent {

  message = '';

  constructor(
    public ref: DynamicDialogRef
  ) {}

  accept(): void {
    this.ref.close(true);
  }

  reject(): void {
    this.ref.close(false);
  }
}