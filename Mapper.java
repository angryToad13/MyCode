@Mapper(componentModel = "spring")
public interface TxnMapper {

    @Mapping(target = "boStatusUpdate", source = "branchCode")
    void mapBoStatusUpdate(ExternalTNTRequest source, @MappingTarget TxnRecord target);

    @Mapping(target = "productCode", source = "prodCode")
    void mapProductCode(ExternalTNTRequest source, @MappingTarget TxnRecord target);

    @Mapping(target = "releaseDttm", source = "releaseDttm")
    void mapReleaseDttm(ExternalTNTRequest source, @MappingTarget TxnRecord target);

    /* ===== Adapter methods ===== */

    default void mapBoStatusUpdate(MappingContext ctx) {
        mapBoStatusUpdate(ctx.getSource(), ctx.getTarget());
    }

    default void mapProductCode(MappingContext ctx) {
        mapProductCode(ctx.getSource(), ctx.getTarget());
    }

    default void mapReleaseDttm(MappingContext ctx) {
        mapReleaseDttm(ctx.getSource(), ctx.getTarget());
    }
}




public class MappingContext {

    private final ExternalTNTRequest source;
    private final TxnRecord target;

    public MappingContext(ExternalTNTRequest source, TxnRecord target) {
        this.source = source;
        this.target = target;
    }

    public ExternalTNTRequest getSource() {
        return source;
    }

    public TxnRecord getTarget() {
        return target;
    }
}




import java.util.function.BiConsumer;

public enum TxnFieldMapping {

    BO_STATUS_UPDATE(TxnMapper::mapBoStatusUpdate),
    PRODUCT_CODE(TxnMapper::mapProductCode),
    RELEASE_DTTM(TxnMapper::mapReleaseDttm);

    private final BiConsumer<TxnMapper, MappingContext> mapperCall;

    TxnFieldMapping(BiConsumer<TxnMapper, MappingContext> mapperCall) {
        this.mapperCall = mapperCall;
    }

    public void apply(TxnMapper mapper, MappingContext context) {
        mapperCall.accept(mapper, context);
    }
}





import java.util.List;

public class TxnFieldMappingRequest {

    private List<TxnFieldMapping> fields;

    public List<TxnFieldMapping> getFields() {
        return fields;
    }

    public void setFields(List<TxnFieldMapping> fields) {
        this.fields = fields;
    }
}




List<String> requestedFields =
        Optional.ofNullable(externalTandTRequest.getFlags())
                .map(Flags::getCommonFields)
                .orElse(Collections.emptyList());

Stream<CommonTxnRecordField> fieldStream =
        requestedFields.isEmpty()
                ? EnumSet.allOf(CommonTxnRecordField.class).stream()
                : requestedFields.stream().map(this::toEnum);



