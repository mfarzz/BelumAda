public class Buku{
    private String kodeBuku;
    private String judulBuku;
    private Integer jumlahBuku;

    public Buku (String kodeBuku, String judulBuku, Integer jumlahBuku){
        this.kodeBuku = kodeBuku;
        this.judulBuku = judulBuku;
        this.jumlahBuku = jumlahBuku;
    }

    public String getKodeBuku() {
        return kodeBuku;
    }

    public void setKodeBuku(String kodeBuku) {
        this.kodeBuku = kodeBuku;
    }

    public String getJudulBuku() {
        return judulBuku;
    }

    public void setJudulBuku(String judulBuku) {
        this.judulBuku = judulBuku;
    }

    public Integer getJumlahBuku() {
        return jumlahBuku;
    }

    public void setJumlahBuku(Integer jumlahBuku) {
        this.jumlahBuku = jumlahBuku;
    }
}
