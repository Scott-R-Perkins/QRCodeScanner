package com.scott.locationtesting;

public class GeoBox {
    //This class is used to determine if the user's location is withing the "box" set by the contains
    //method. this may need to change based on what corners we use for the box. Consult the 3rd/5th member
    //when looking to make changes.
    private final double swLat;
    private final double swLong;
    private final double neLat;
    private final double neLong;

    public GeoBox(double swLat, double swLong, double neLat, double neLong, double bufferInMeters) {
        double bufferInDegrees = bufferInMeters / 111000;

        this.swLat = swLat - bufferInDegrees;
        this.swLong = swLong - bufferInDegrees;
        this.neLat = neLat + bufferInDegrees;
        this.neLong = neLong + bufferInDegrees;
    }

    public boolean contains(double lat, double lon) {
        return lat >= swLat && lat <= neLat && lon >= swLong && lon <= neLong;
    }
}

