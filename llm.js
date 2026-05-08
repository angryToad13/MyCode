this.formGroup.valueChanges
  .pipe(
    debounceTime(100),
    map(values => this.computeTotals(values))
  )
  .subscribe(totals => {
    this.totalSubject.next(totals.total);
    this.copyTotalSubject.next(totals.copy_count);
    this.originalTotalSubject.next(totals.original_count);
  });