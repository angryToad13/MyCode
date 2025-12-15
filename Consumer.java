public enum CommonField {
    BRANCH_CODE,
    BO_REF_ID,
    TXN_ID,
    TXN_TYPE_CODE,
    TXN_STAT_CODE,
    BO_STATUS_UPDATE,
    SUB_TXN_STAT_CODE,
    PRODUCT_CODE,
    RELEASE_DTTM
}

default void setCommonFields(
        T txnRecord,
        ExternalTandTRequest req,
        Set<CommonField> fieldsToSet) {

    for (CommonField field : fieldsToSet) {
        switch (field) {

            case BRANCH_CODE ->
                txnRecord.setBrch_code(req.getBranchCode());

            case BO_REF_ID ->
                txnRecord.setBo_ref_id(
                    req.getConnexisRequestRefId() != null
                        ? req.getEventId()
                        : null
                );

            case TXN_ID ->
                txnRecord.setTxn_id(req.getConnexisRequestTxnId());

            case TXN_TYPE_CODE ->
                txnRecord.setTxn_type_code(
                    req.getConnexisRequestTxnId() == null
                        ? req.getTxnTypeCode()
                        : null
                );

            case TXN_STAT_CODE ->
                txnRecord.setTxn_stat_code(
                    req.getConnexisRequestTxnId() == null
                        ? req.getTxnStatCode()
                        : null
                );

            case BO_STATUS_UPDATE ->
                txnRecord.setBo_status_update(req.getBoStatusUpdate());

            case SUB_TXN_STAT_CODE ->
                txnRecord.setSub_txn_stat_code(req.getSubTxnStatCode());

            case PRODUCT_CODE ->
                txnRecord.setProduct_code(req.getProdCode());

            case RELEASE_DTTM ->
                txnRecord.setBo_release_dttm(DateTimeUtil.getCurrentTimeInCET());
        }
    }
}

⸻

3️⃣ Pass only the fields you want
Set<CommonField> fields =
        EnumSet.of(
            CommonField.BRANCH_CODE,
            CommonField.TXN_ID,
            CommonField.PRODUCT_CODE
        );

xmlStrategy.setCommonFields(txnRecord, request, fields);

DEFAULT BEHAVE

default Set<CommonField> defaultFields() {
    return EnumSet.of(
        CommonField.BRANCH_CODE,
        CommonField.TXN_ID,
        CommonField.RELEASE_DTTM
    );
}


public static <E extends Enum<E>> Set<E> fromStringArraySafe(
        String[] values, Class<E> enumClass) {

    Set<E> result = EnumSet.noneOf(enumClass);

    if (values == null) {
        return result;
    }

    for (String value : values) {
        try {
            result.add(Enum.valueOf(enumClass, value.trim().toUpperCase()));
        } catch (Exception ignored) {
            // optionally log here
        }
    }
    return result;
}