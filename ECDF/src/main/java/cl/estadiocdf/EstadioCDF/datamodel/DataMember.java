package cl.estadiocdf.EstadioCDF.datamodel;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface DataMember {
    String member ();
}
