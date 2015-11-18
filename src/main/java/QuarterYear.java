import java.util.Calendar;

public class QuarterYear {
    public static final int Q1 = 4;
    public static final int Q2 = 1;
    public static final int Q3 = 2;
    public static final int Q4 = 3;

    private int quarter;
    private int year;

    QuarterYear() {
        Calendar cal = Calendar.getInstance();
        int month = cal.get(Calendar.MONTH);
        this.year = cal.get(Calendar.YEAR);
        if (month>=9 && month<=11) {
            this.quarter = QuarterYear.Q1;
        }else if (month>=12 || month<=2) {
            this.quarter = QuarterYear.Q2;
        }else if (month>=3 && month<=5) {
            this.quarter = QuarterYear.Q3;
        }else {
            this.quarter = QuarterYear.Q4;
        }
    }

    QuarterYear(int quarter, int year) {
        this.quarter = quarter;
        this.year = year;
    }

    public QuarterYear nextQuarterYear(){
        int nextQuarter = 1+(this.quarter)%4;
        int nextYear = this.year + (this.quarter)/4;
        return new QuarterYear(nextQuarter,nextYear);
    }

    public String quarterToString(){
        if (this.quarter == QuarterYear.Q1){
            return "Q1";
        }else if (this.quarter == QuarterYear.Q2) {
            return "Q2";
        }else if (this.quarter == QuarterYear.Q3) {
            return "Q3";
        }else {
            return "Q4";
        }

    }

    public int getYear(){
        return (this.year);
    }

}
