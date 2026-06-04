INSERT INTO DPW_DOCUMENT (
    DOC_KEY,
    NAME,
    TITLE,
    DOC_CATEGORY,
    DOC_ATTACHMENT_DATE,
    AUTHOR,
    DOC_CLASSIFICATION,
    DOC_FILE_EXTENSION,
    DOC_RECEPTION_TO_BANK_DATE,
    DOC_RECEPTION_SYSTEM_DATE,
    DOC_SOURCE,
    DOC_COMMENTS,
    DOC_REF_ID,
    REF_REQUEST_ID,
    REF_EVENT_ID,
    CONTAINER_KEY,
    ANNOTATION_DOC_KEY,
    VERSION,
    KEEP_ORIGINAL_FILENAME,
    SWIFT_STATUS,
    ELIGIBLE_FOR_TRADEAI,
    DOCUMENT_TRADEAI_STATUS,
    DOCUMENT_SIZE,
    DOCUMENT_VERSION,
    MUR_CODE,
    SEND_TO_CONNEXIS,
    CONNEXIS_STATUS,
    SAA_FILE
)
SELECT
    'DOC_' || LEVEL,
    'Document ' || LEVEL,
    'Title ' || LEVEL,
    CASE MOD(LEVEL,3)
        WHEN 0 THEN 'PDF'
        WHEN 1 THEN 'SWIFT'
        ELSE 'XML'
    END,
    SYSTIMESTAMP - DBMS_RANDOM.VALUE(0,365),
    'SYSTEM',
    'PUBLIC',
    CASE MOD(LEVEL,3)
        WHEN 0 THEN 'pdf'
        WHEN 1 THEN 'txt'
        ELSE 'xml'
    END,
    SYSTIMESTAMP - DBMS_RANDOM.VALUE(0,365),
    SYSTIMESTAMP,
    'UPLOAD',
    'Dummy Data',
    'REF_' || LEVEL,
    'REQ_' || LEVEL,
    'EVENT_' || MOD(LEVEL,10000),

    (
        SELECT container_key
        FROM (
            SELECT container_key
            FROM dpw_container
            ORDER BY DBMS_RANDOM.VALUE
        )
        WHERE ROWNUM = 1
    ),

    NULL,
    1,
    'file_' || LEVEL || '.pdf',
    'READY',
    'Y',
    'COMPLETED',
    ROUND(DBMS_RANDOM.VALUE(1000,15000000)),
    1,
    'MUR' || MOD(LEVEL,1000),
    'Y',
    'SENT',
    'N'
FROM dual
CONNECT BY LEVEL <= 1000000;

COMMIT;