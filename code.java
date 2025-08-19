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

import org.springframework.data.jpa.domain.Specification;

public class EventSpecifications {

    // Common filter: branchCode + countryCode
    public static Specification<EventEntity> branchAndCountry(String branchCode, String countryCode) {
        return (root, query, cb) -> cb.and(
                cb.equal(root.get("branchCode"), branchCode),
                cb.equal(root.get("countryCode"), countryCode)
        );
    }

    // Generic filter for "field = value"
    public static Specification<EventEntity> equalsField(String fieldName, String value) {
        return (root, query, cb) -> cb.equal(root.get(fieldName), value);
    }

    // Exclude statuses
    public static Specification<EventEntity> statusNotIn(List<String> statuses) {
        return (root, query, cb) -> cb.not(root.get("status").in(statuses));
    }
}