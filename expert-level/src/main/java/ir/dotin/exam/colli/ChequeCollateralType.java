package ir.dotin.exam.colli;

import java.util.Set;

public class ChequeCollateralType extends CollateralType implements ExclusiveGroup{

    public ChequeCollateralType(Set<CollateralCapability> capabilitySet) {
        super(capabilitySet);
    }

    @Override
    protected Set<CapabilityType> allowed() {
        return Set.of(
                CapabilityType.AUTO_RELEASE,
                CapabilityType.ESCROW
        );
    }



    @Override
    protected void validateSpecific() {
        // business rules
    }

    @Override
    public Set<Set<CapabilityType>> exclusiveGroups() {
        return Set.of();
    }
}
