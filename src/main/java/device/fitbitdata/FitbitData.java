package device.fitbitdata;

public abstract class FitbitData {
    private long millisec = 0;

    public void setDate(long millisec) { this.millisec = millisec; }
    public long getDate() { return this.millisec; }
}
