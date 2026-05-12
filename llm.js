import {
  AfterViewInit,
  Directive,
  ElementRef,
  Input,
  Renderer2
} from '@angular/core';

@Directive({
  selector: '[appCopyableTable]'
})
export class CopyTableDirective
  implements AfterViewInit {

  @Input('appCopyableTable')
  tableData: any[] = [];

  @Input()
  copyColumns: any[] = [];

  constructor(
    private el: ElementRef,
    private renderer: Renderer2
  ) {}

  ngAfterViewInit(): void {

    const wrapper =
      this.renderer.createElement('div');

    this.renderer.setStyle(
      wrapper,
      'display',
      'flex'
    );

    this.renderer.setStyle(
      wrapper,
      'justify-content',
      'flex-end'
    );

    this.renderer.setStyle(
      wrapper,
      'margin-bottom',
      '10px'
    );

    const button =
      this.renderer.createElement('button');

    button.innerHTML = `
      <span class="pi pi-copy"></span>
      Copy Table
    `;

    this.renderer.addClass(
      button,
      'p-button'
    );

    this.renderer.addClass(
      button,
      'p-component'
    );

    this.renderer.setStyle(
      button,
      'padding',
      '6px 12px'
    );

    this.renderer.listen(
      button,
      'click',
      () => {
        this.copyTable();
      }
    );

    this.renderer.appendChild(
      wrapper,
      button
    );

    const parent =
      this.el.nativeElement.parentNode;

    this.renderer.insertBefore(
      parent,
      wrapper,
      this.el.nativeElement
    );

  }

  async copyTable() {

    let html = `
      <table
        border="1"
        style="
          border-collapse: collapse;
          width: 100%;
          font-family: Arial;
        "
      >
    `;

    html += `
      <tr style="background:#f2f2f2;">
    `;

    this.copyColumns.forEach(col => {

      html += `
        <th
          style="
            padding:10px;
            min-width:${col.width || '120px'};
            text-align:${col.align || 'left'};
          "
        >
          ${col.header}
        </th>
      `;

    });

    html += `</tr>`;

    this.tableData.forEach(row => {

      html += `<tr>`;

      this.copyColumns.forEach(col => {

        let value =
          row[col.field];

        if (
          value === null ||
          value === undefined
        ) {
          value = '';
        }

        html += `
          <td
            style="
              padding:10px;
              text-align:${col.align || 'left'};
            "
          >
            ${value}
          </td>
        `;

      });

      html += `</tr>`;

    });

    html += `</table>`;

    await navigator.clipboard.write([
      new ClipboardItem({
        'text/html': new Blob(
          [html],
          {
            type: 'text/html'
          }
        )
      })
    ]);

  }

}