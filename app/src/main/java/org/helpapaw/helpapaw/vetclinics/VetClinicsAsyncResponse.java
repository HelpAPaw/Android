package org.helpapaw.helpapaw.vetclinics;

import java.util.HashMap;
import java.util.List;

public interface VetClinicsAsyncResponse {
    void vetClinicsLoaded(List<HashMap<String, String>> result);
}
