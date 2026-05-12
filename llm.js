copyTable() {

  let html = `
    <table border="1" style="border-collapse: collapse;">
      <tr>
        <th>Document</th>
        <th>Original</th>
        <th>Copy</th>
        <th>Photocopy</th>
        <th>Scanned</th>
      </tr>
  `;

  this.users.forEach(row => {

    html += `
      <tr>
        <td>${row.document}</td>
        <td>${row.original}</td>
        <td>${row.copy}</td>
        <td>${row.photocopy}</td>
        <td>${row.scanned}</td>
      </tr>
    `;

  });

  html += `</table>`;

  const blob = new Blob(
    [html],
    { type: 'text/html' }
  );

  const data = [
    new ClipboardItem({
      'text/html': blob
    })
  ];

  navigator.clipboard.write(data);

}