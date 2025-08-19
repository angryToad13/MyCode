public class EventFieldMapping {
    public static final Map<String, String> FIELD_MAP = Map.of(
            "CXT_TXN_ID", "connexisRequestTxnId",
            "CXT_REF_ID", "connexisRequestRefId",
            "UMFR",       "umfr",
            "MFR",        "mfr"
    );
}



for (Map.Entry<String, String> entry : EventFieldMapping.FIELD_MAP.entrySet()) {
    String foundBy = entry.getKey();
    String columnName = entry.getValue();

    // Get the corresponding value dynamically
    String value = switch (foundBy) {
        case "CXT_TXN_ID" -> cxtTxnId;
        case "CXT_REF_ID" -> cxtRefId;
        case "UMFR"       -> umfr;
        case "MFR"        -> mfr;
        default           -> null;
    };

    if (value == null) continue;

    Specification<EventEntity> spec = Specification
            .where(EventSpecifications.branchAndCountry(branchCode, countryCode))
            .and(EventSpecifications.equalsField(columnName, value));

    if (isAP) {
        spec = spec.and(EventSpecifications.statusNotIn(excludedStatuses));
    }

    Sort sort = Sort.by(Sort.Direction.DESC, "systemCreationDate");
    eventEntity = eventRepository.findOne(spec, sort).orElse(null);

    if (eventEntity != null) {
        reqEventDetails.setFoundByField(foundBy);
        break;
    }
}