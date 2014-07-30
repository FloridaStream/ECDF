package cl.estadiocdf.EstadioCDF.serializables;

import java.io.Serializable;

import cl.estadiocdf.EstadioCDF.datamodel.LiveStreamSchedule;
import cl.estadiocdf.EstadioCDF.datamodel.Media;

/**
 * Created by Boris on 22-04-14.
 */
public class MediaSerializable implements Serializable {
    private Media media_Media = null;
    private LiveStreamSchedule media_LiveStreamSchedule = null;

    public void setMedia(Media media){
        media_Media = media;
    }

    public Media getMedia() {
        return media_Media;
    }

    public void setLiveStreamSchedule(LiveStreamSchedule liveStreamSchedule){
        media_LiveStreamSchedule = liveStreamSchedule;
    }

    public LiveStreamSchedule getLiveStreamSchedule() {
        return media_LiveStreamSchedule;
    }

}
