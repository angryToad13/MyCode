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



