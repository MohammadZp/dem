package ir.dotin.exam.colli;

public class AutoReleaseCapability implements CollateralCapability {

    private Integer days;

    @Override
    public CapabilityType type() {
        return CapabilityType.AUTO_RELEASE;
    }

    @Override
    public void validate() {
        if (days == null) {
            throw new IllegalArgumentException("asdas");
        }
    }
}
