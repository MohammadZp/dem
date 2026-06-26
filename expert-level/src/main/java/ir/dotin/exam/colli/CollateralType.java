package ir.dotin.exam.colli;

import java.util.Set;

import java.util.Set;

public abstract class CollateralType {

    private final Set<CollateralCapability> capabilitySet;

    protected CollateralType(Set<CollateralCapability> capabilitySet) {
        this.capabilitySet = capabilitySet;
    }

    protected abstract Set<CapabilityType> allowed();
    protected abstract void validateSpecific();


    public final void validate() {

        validateAllowedCapability();
        validateMutualExclusion();

        validateSpecific();

        for (CollateralCapability cap : capabilitySet) {
            cap.validate();
        }
    }

    private void validateAllowedCapability() {

        Set<CapabilityType> allowed = allowed();

        for (CollateralCapability cap : capabilitySet) {
            if (!allowed.contains(cap.type())) {
                throw new IllegalArgumentException(
                        "Not allowed: " + cap.type()
                );
            }
        }
    }

    private void validateMutualExclusion() {

        Set<CapabilityType> selected =
                capabilitySet.stream()
                        .map(CollateralCapability::type)
                        .collect(java.util.stream.Collectors.toSet());

        if(this instanceof ExclusiveGroup) {
            Set<Set<CapabilityType>> exclusiveGroup= ((ExclusiveGroup) this).exclusiveGroups();
            for (Set<CapabilityType> group : exclusiveGroup) {

                long count = group.stream()
                        .filter(selected::contains)
                        .count();

                if (count > 1) {
                    throw new IllegalArgumentException(
                            "Mutually exclusive capabilities violated: " + group
                    );
                }
            }
        }
    }

}


