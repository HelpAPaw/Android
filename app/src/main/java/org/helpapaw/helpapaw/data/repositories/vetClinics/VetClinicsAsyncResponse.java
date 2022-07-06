package org.helpapaw.helpapaw.data.repositories.vetClinics;

import org.helpapaw.helpapaw.data.models.VetClinic;

import java.util.List;

public interface VetClinicsAsyncResponse {
    void vetClinicsLoaded(List<VetClinic> result);
}
