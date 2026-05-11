copyTable() {

  const headers = this.cols.map(c => c.header);

  const rows = this.users.map(row =>
    this.cols.map(col => row[col.field])
  );

  const text = [
    headers.join('\t'),
    ...rows.map(r => r.join('\t'))
  ].join('\n');

  navigator.clipboard.writeText(text);

}