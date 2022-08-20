package org.helpapaw.helpapaw.data.repositories.vetClinics;

import org.helpapaw.helpapaw.data.models.VetClinic;

public interface VetClinicDetailsAsyncResponse {
    void onVetClinicDetailsSuccess(VetClinic result);
    void onVetClinicDetailsFailure(String error);
}
