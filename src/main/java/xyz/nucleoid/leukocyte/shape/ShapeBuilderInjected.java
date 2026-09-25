package xyz.nucleoid.leukocyte.shape;

public interface ShapeBuilderInjected {
    void leukocyte$start();

    void leukocyte$add(ProtectionShape shape);

    ProtectionShape leukocyte$finish();

    boolean leukocyte$isBuilding();

    default ShapeBuilder leukocyte$getAsBuilder() {
        return new ShapeBuilder() {
            @Override
            public void start() {
                ShapeBuilderInjected.this.leukocyte$start();
            }

            @Override
            public void add(ProtectionShape shape) {
                ShapeBuilderInjected.this.leukocyte$add(shape);
            }

            @Override
            public ProtectionShape finish() {
                return ShapeBuilderInjected.this.leukocyte$finish();
            }

            @Override
            public boolean isBuilding() {
                return ShapeBuilderInjected.this.leukocyte$isBuilding();
            }
        };
    }
}
