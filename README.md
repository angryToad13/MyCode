public Map<String, List<EventTree>> buildSortedEventTreeMap(List<EventEntity> eventEntityList) {
    return eventEntityList.stream()
        .collect(Collectors.groupingBy(
            e -> e.getMfr() + "-" + e.getUmfr(), // key: mfr + umfr
            Collectors.collectingAndThen(
                Collectors.toList(),
                list -> list.stream()
                    .sorted(Comparator.comparing(EventEntity::getSystemCreationDate))
                    .map(this::convertToEventTree) // convert EventEntity to EventTree
                    .collect(Collectors.toList())
            )
        ));
}

// Sample converter function (you can adjust it to your actual logic)
private EventTree convertToEventTree(EventEntity entity) {
    EventTree tree = new EventTree();
    tree.setEventId(entity.getId());
    tree.setUmfr(entity.getUmfr());
    tree.setStarted(entity.getSystemCreationDate());
    // set other fields as needed
    return tree;
}