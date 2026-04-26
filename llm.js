function mapToRequest(llmResponse, userResponse) {
  const result = [];

  // Create lookup maps for fast access
  const llmMap = new Map(
    llmResponse.map(item => [item.document_type, item])
  );

  const userMap = new Map(
    userResponse.map(item => [item.document_type, item])
  );

  // Create a unique set of all document types
  const allTypes = new Set([
    ...llmMap.keys(),
    ...userMap.keys()
  ]);

  allTypes.forEach(type => {
    const llmItem = llmMap.get(type);
    const userItem = userMap.get(type);

    result.push({
      userDocumentType: userItem?.document_type || type,
      userCopyCount: userItem?.copy_count ?? 0,
      userOriginalCount: userItem?.original_count ?? 0,

      llmDocumentType: llmItem?.document_type || null,
      llmCopyCount: llmItem?.copy_count ?? 0,
      llmOriginalCount: llmItem?.original_count ?? 0
    });
  });

  return result;
}