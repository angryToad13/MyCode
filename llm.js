cols = [
  {
    field: 'document',
    header: 'Document',
    width: '160px',
    align: 'left'
  },
  {
    field: 'original',
    header: 'Original',
    width: '120px',
    align: 'center'
  },
  {
    field: 'copy',
    header: 'Copy',
    width: '120px',
    align: 'center'
  },
  {
    field: 'photocopy',
    header: 'Photocopy',
    width: '140px',
    align: 'center'
  },
  {
    field: 'scanned',
    header: 'Scanned',
    width: '120px',
    align: 'center'
  }
];

users = [
  {
    document: 'Passport',
    original: 2,
    copy: 4,
    photocopy: 1,
    scanned: 3
  },
  {
    document: 'PAN Card',
    original: 1,
    copy: 2,
    photocopy: 2,
    scanned: 1
  }
];

copyTable() {

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

  html += `<tr style="background:#f2f2f2;">`;

  this.cols.forEach(col => {

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

  this.users.forEach(row => {

    html += `<tr>`;

    this.cols.forEach(col => {

      html += `
        <td
          style="
            padding:10px;
            text-align:${col.align || 'left'};
          "
        >
          ${row[col.field] ?? ''}
        </td>
      `;

    });

    html += `</tr>`;

  });

  html += `</table>`;

  navigator.clipboard.write([
    new ClipboardItem({
      'text/html': new Blob(
        [html],
        { type: 'text/html' }
      )
    })
  ]);

}