package cl.estadiocdf.EstadioCDF.delegates;

import cl.estadiocdf.EstadioCDF.datamodel.LiveStreamSchedule;
import cl.estadiocdf.EstadioCDF.datamodel.Media;

/**
 * Created by Franklin Cruz on 14-03-14.
 */
public abstract class VideoDelegate {

    public void onVideoSelected(Media media) {

    }

    public void onLiveShowBegin(LiveStreamSchedule media) {

    }

    public void displayImageChooser(String image1, String image2, ImageChooserDelegate delegate) {

    }

    public void onVideoSelected(LiveStreamSchedule media) {

    }
}
