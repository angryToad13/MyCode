import {
  AfterViewInit,
  Directive,
  ElementRef,
  Renderer2
} from '@angular/core';

@Directive({
  selector: '[appCopyableTable]'
})
export class CopyableTableDirective
  implements AfterViewInit {

  constructor(
    private el: ElementRef,
    private renderer: Renderer2
  ) {}

  ngAfterViewInit(): void {

    const wrapper = this.renderer.createElement('div');

    this.renderer.setStyle(wrapper, 'display', 'flex');
    this.renderer.setStyle(wrapper, 'justify-content', 'flex-end');
    this.renderer.setStyle(wrapper, 'margin-bottom', '10px');

    const button = this.renderer.createElement('button');

    button.innerHTML = `
      <span class="pi pi-copy"></span>
      Copy Table
    `;

    this.renderer.addClass(button, 'p-button');
    this.renderer.addClass(button, 'p-component');

    this.renderer.setStyle(button, 'padding', '6px 12px');
    this.renderer.setStyle(button, 'cursor', 'pointer');

    this.renderer.listen(
      button,
      'click',
      () => this.copyTable()
    );

    this.renderer.appendChild(wrapper, button);

    const parent =
      this.el.nativeElement.parentNode;

    this.renderer.insertBefore(
      parent,
      wrapper,
      this.el.nativeElement
    );

  }

  async copyTable() {

    const table: HTMLTableElement =
      this.el.nativeElement.querySelector('table');

    if (!table) {
      return;
    }

    const clonedTable =
      table.cloneNode(true) as HTMLTableElement;

    clonedTable.style.borderCollapse =
      'collapse';

    clonedTable.style.width = '100%';

    clonedTable.style.fontFamily =
      'Arial';

    clonedTable
      .querySelectorAll('th, td')
      .forEach((cell: any) => {

        cell.style.border =
          '1px solid #d1d5db';

        cell.style.padding =
          '10px';

        cell.style.minWidth =
          '120px';

      });

    const html = clonedTable.outerHTML;

    await navigator.clipboard.write([
      new ClipboardItem({
        'text/html': new Blob(
          [html],
          { type: 'text/html' }
        )
      })
    ]);

  }

}