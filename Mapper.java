public List<Customer> getCustomerByProperty(
        String property,
        String value) throws BatchException {

    Map<String, String> params = new HashMap<>();

    params.put("branchCode", null);
    params.put("countryCode", null);
    params.put("customerName", null);
    params.put("customerId", null);
    params.put("currency", null);

    if (!params.containsKey(property)) {
        throw new IllegalArgumentException(
                "Unsupported property: " + property);
    }

    params.put(property, value);

    return getCustomerIdOrCustomerAccount(
            params.get("branchCode"),
            params.get("countryCode"),
            params.get("customerName"),
            params.get("customerId"),
            params.get("currency"));
}