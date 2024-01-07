import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

public class BukuKomik extends Buku implements PeminjamaPengembalian, Stok {
    private String jenisKomik;
    private List<BukuKomik> stokBukuKomik;

    public BukuKomik(String kodeBuku, String judulBuku, Integer jumlahBuku, String jenisKomik) {
        super(kodeBuku, judulBuku, jumlahBuku);
        this.jenisKomik = jenisKomik;
        this.stokBukuKomik = new ArrayList<>();
    }

    public String getJenisKomik() {
        return jenisKomik;
    }

    public void setJenisKomik(String jenisKomik) {
        this.jenisKomik = jenisKomik;
    }

    @Override
public void tambahStok(Scanner scanner) {
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/pbo", "root", "")) {
            boolean lanjutInput = true;
            while (lanjutInput) {
                System.out.print("Masukkan Kode Buku\t: ");
                String kodeBuku = scanner.next();

                if (isKodeBukuExists(connection, kodeBuku)) {
                    System.out.println("Error: Kode Buku sudah ada. Silakan masukkan kode buku yang lain.");
                    continue;
                }

                System.out.print("Masukkan Judul Buku\t: ");
                String judulBuku = scanner.next();
                Integer jumlahBuku = 0;
                boolean inputJumlahValid = false;
                while (!inputJumlahValid) {
                    try {
                        System.out.print("Masukkan Jumlah Buku\t: ");
                        jumlahBuku = scanner.nextInt();
                        if (jumlahBuku <= 0) {
                            throw new ArithmeticException("Jumlah barang harus lebih dari 0.");
                        }
                        inputJumlahValid = true;
                    } catch (InputMismatchException e) {
                        System.out.println("Error: Input jumlah barang tidak valid. Silakan coba lagi.");
                        scanner.next();
                    } catch (ArithmeticException e) {
                        System.out.println("Error: " + e.getMessage());
                    }
                }
                System.out.print("Masukkan Jenis Komik\t: ");
                String jenisKomik = scanner.next();
                System.out.println("Data Telah Ditambahkan");
                BukuKomik bukuKomik = new BukuKomik(kodeBuku, judulBuku, jumlahBuku, jenisKomik);

                insertIntoDatabase(bukuKomik);

                System.out.print("Apakah ingin menambahkan stok lagi? (y/n): ");
                String input = scanner.next();
                lanjutInput = input.equalsIgnoreCase("y");

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private boolean isKodeBukuExists(Connection connection, String kodeBuku) throws SQLException {
        String query = "SELECT COUNT(*) FROM komik WHERE kodeBuku = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, kodeBuku);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    int count = resultSet.getInt(1);
                    return count > 0;
                }
            }
        }
        return false;
    }

    private void insertIntoDatabase(BukuKomik bukuKomik) {
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/pbo", "root", "")) {
            Date tanggal = new Date();

            String query = "INSERT INTO komik (tanggal, kodeBuku, judulBuku, jumlahBuku, jenisKomik) "
                    + "VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setDate(1, new java.sql.Date(tanggal.getTime()));
                preparedStatement.setString(2, bukuKomik.getKodeBuku());
                preparedStatement.setString(3, bukuKomik.getJudulBuku());
                preparedStatement.setInt(4, bukuKomik.getJumlahBuku());
                preparedStatement.setString(5, bukuKomik.getJenisKomik());

                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void tambahStokBaru(Scanner scanner) {
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/pbo", "root", "")) {
            boolean lanjutInput = true;
            while (lanjutInput) {
                System.out.print("Masukkan Kode Buku\t: ");
                String kodeBuku = scanner.next();

                if (!isKodeBukuExists(connection, kodeBuku)) {
                    System.out.println("Error: Kode Buku tidak ditemukan. Silakan masukkan kode buku yang valid.");
                    return;
                }

                Integer jumlahTambah = 0;
                boolean inputJumlahValid = false;
                while (!inputJumlahValid) {
                    try {
                        System.out.print("Masukkan Jumlah Buku\t: ");
                        jumlahTambah = scanner.nextInt();
                        if (jumlahTambah <= 0) {
                            throw new ArithmeticException("Jumlah barang yang ditambahkan harus lebih dari 0.");
                        }
                        inputJumlahValid = true;
                    } catch (InputMismatchException e) {
                        System.out.println("Error: Input jumlah barang tidak valid. Silakan coba lagi.");
                        scanner.next();
                    } catch (ArithmeticException e) {
                        System.out.println("Error: " + e.getMessage());
                    }
                }

                updateStokInDatabase(connection, kodeBuku, getCurrentStock(connection, kodeBuku) + jumlahTambah);

                System.out.println("Stok berhasil ditambahkan.");
                System.out.print("Apakah ingin menambahkan stok lagi? (y/n): ");
                String input = scanner.next();
                lanjutInput = input.equalsIgnoreCase("y");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void displayStok() {
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/pbo", "root", "")) {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM komik");
            ResultSet resultSet = statement.executeQuery();

            if (!resultSet.isBeforeFirst()) {
                System.out.println("Tidak ada stok buku");
            } else {
                System.out.println("Stok Buku\t:");
                System.out.println(
                        "--------------------------------------------------------------------------");
                System.out.printf("| %-15s | %-15s | %-15s | %-15s |\n",
                        "Kode Buku", "Judul Buku", "Jumlah Buku", "Jenis Komik");
                System.out.println(
                        "--------------------------------------------------------------------------");
                while (resultSet.next()) {
                    String kodeBuku = resultSet.getString("kodeBuku");
                    String judulBuku = resultSet.getString("judulBuku");
                    Integer jumlahBuku = resultSet.getInt("jumlahBuku");
                    String jenisKomik = resultSet.getString("jenisKomik");

                    System.out.printf("| %-15s | %-15s | %-15s | %-15s |\n",
                            kodeBuku, judulBuku, jumlahBuku, jenisKomik);

                    BukuKomik bukuKomik = new BukuKomik(kodeBuku, judulBuku, jumlahBuku, jenisKomik);
                    stokBukuKomik.add(bukuKomik);
                }
                System.out.println(
                        "-----------------------------------------------------------------------------------------------");
            }
            statement.close();
            resultSet.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void kurangStok(Scanner scanner) {
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/pbo", "root", "")) {
            boolean lanjutInput = true;
            while (lanjutInput) {
                System.out.print("Masukkan Kode Buku\t: ");
                String kodeBuku = scanner.next();

                if (!isKodeBukuExists(connection, kodeBuku)) {
                    System.out.println("Error: Kode Buku tidak ditemukan. Silakan masukkan kode buku yang valid.");
                    continue;
                }

                Integer jumlahKurang = 0;
                boolean inputJumlahValid = false;
                Integer currentStock = getCurrentStock(connection, kodeBuku);
                while (!inputJumlahValid) {
                    try {
                        System.out.print("Masukkan Jumlah Buku\t: ");
                        jumlahKurang = scanner.nextInt();
                        if (jumlahKurang <= 0) {
                            throw new ArithmeticException("Jumlah barang yang ingin dikurangi harus lebih dari 0.");
                        }
                        if (currentStock < jumlahKurang) {
                            System.out.println("Error: Jumlah buku yang ingin dikurangi melebihi stok yang tersedia.");
                            continue;
                        }
                        inputJumlahValid = true;
                    } catch (InputMismatchException e) {
                        System.out.println("Error: Input jumlah barang tidak valid. Silakan coba lagi.");
                        scanner.next();
                    } catch (ArithmeticException e) {
                        System.out.println("Error: " + e.getMessage());
                    }
                }

                updateStokInDatabase(connection, kodeBuku, currentStock - jumlahKurang);

                System.out.println("Stok berhasil dikurangi.");
                System.out.print("Apakah ingin mengurangi stok lagi? (y/n): ");
                String input = scanner.next();
                lanjutInput = input.equalsIgnoreCase("y");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Integer getCurrentStock(Connection connection, String kodeBuku) throws SQLException {
        String query = "SELECT jumlahBuku FROM komik WHERE kodeBuku = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, kodeBuku);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("jumlahBuku");
                }
            }
        }
        return 0;
    }

    private void updateStokInDatabase(Connection connection, String kodeBuku, Integer newStock) throws SQLException {
        String query = "UPDATE komik SET jumlahBuku = ? WHERE kodeBuku = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, newStock);
            preparedStatement.setString(2, kodeBuku);
            preparedStatement.executeUpdate();
        }
    }

    @Override
    public void pinjamBuku(Scanner scanner) {
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/pbo", "root", "")) {
            boolean lanjutInput = true;
            while (lanjutInput) {
                System.out.print("Masukkan Nama Peminjam\t\t: ");
                String namaPeminjam = scanner.next();

                System.out.print("Masukkan Kode Buku yang Dipinjam\t: ");
                String kodeBuku = scanner.next();

                if (!isKodeBukuExists(connection, kodeBuku)) {
                    System.out.println("Error: Kode Buku tidak ditemukan. Silakan masukkan kode buku yang valid.");
                    continue;
                }

                Integer jumlahPinjam = 0;
                boolean inputJumlahValid = false;
                Integer currentStock = getCurrentStock(connection, kodeBuku);
                while (!inputJumlahValid) {
                    try {
                        System.out.print("Masukkan Jumlah Buku yang Dipinjam\t: ");
                        jumlahPinjam = scanner.nextInt();
                        if (jumlahPinjam <= 0) {
                            throw new ArithmeticException("Jumlah buku yang dipinjam harus lebih dari 0.");
                        }
                        if (currentStock < jumlahPinjam) {
                            System.out.println("Error: Jumlah buku yang ingin dipinjam melebihi stok yang tersedia.");
                            continue;
                        }
                        inputJumlahValid = true;
                    } catch (InputMismatchException e) {
                        System.out.println("Error: Input jumlah buku tidak valid. Silakan coba lagi.");
                        scanner.next();
                    } catch (ArithmeticException e) {
                        System.out.println("Error: " + e.getMessage());
                    }
                }

                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                Date tanggalPeminjaman = new Date();
                Date tanggalPengembalian = calculateReturnDate(tanggalPeminjaman);

                System.out.println("Tanggal Peminjaman\t\t: " + dateFormat.format(tanggalPeminjaman));
                System.out.println("Maksimal Tanggal Pengembalian\t: " + dateFormat.format(tanggalPengembalian));

                String judulBuku = getJudulBukuFromDatabase(connection, kodeBuku);

                recordPeminjaman(connection, namaPeminjam, kodeBuku, judulBuku, jumlahPinjam, tanggalPeminjaman,
                        tanggalPengembalian);

                System.out.println("Peminjaman berhasil dilakukan.");
                System.out.print("Apakah ingin melakukan peminjaman lagi? (y/n): ");
                String input = scanner.next();
                lanjutInput = input.equalsIgnoreCase("y");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Date calculateReturnDate(Date borrowingDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(borrowingDate);
        calendar.add(Calendar.DAY_OF_YEAR, 7);
        return calendar.getTime();
    }

    private void recordPeminjaman(Connection connection, String namaPeminjam, String kodeBuku, String judulBuku,
            Integer jumlahPinjam, Date tanggalPeminjaman, Date tanggalPengembalian) throws SQLException {
        String query = "INSERT INTO peminjaman (namaPeminjam, kodeBuku, judulBuku, jumlahBuku, tanggalPinjam, tanggalKembali, status) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, namaPeminjam);
            preparedStatement.setString(2, kodeBuku);
            preparedStatement.setString(3, judulBuku);
            preparedStatement.setInt(4, jumlahPinjam);
            preparedStatement.setDate(5, new java.sql.Date(tanggalPeminjaman.getTime()));
            preparedStatement.setDate(6, new java.sql.Date(tanggalPengembalian.getTime()));
            preparedStatement.setString(7, "Dipinjam");

            preparedStatement.executeUpdate();

            updateStokInDatabase(connection, kodeBuku, getStockFromDatabase(connection, kodeBuku) - jumlahPinjam);
        }
    }

    private Integer getStockFromDatabase(Connection connection, String kodeBuku) throws SQLException {
        String query = "SELECT jumlahBuku FROM komik WHERE kodeBuku = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, kodeBuku);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("jumlahBuku");
                }
            }
        }
        return 0;
    }

    private String getJudulBukuFromDatabase(Connection connection, String kodeBuku) throws SQLException {
        String query = "SELECT judulBuku FROM komik WHERE kodeBuku = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, kodeBuku);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString("judulBuku");
                }
            }
        }
        return "";
    }

    private boolean isNamaPeminjamExists(Connection connection, String namaPeminjam) throws SQLException {
        String query = "SELECT COUNT(*) FROM peminjaman WHERE namaPeminjam = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, namaPeminjam);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    int count = resultSet.getInt(1);
                    return count > 0;
                }
            }
        }
        return false;
    }

    @Override
    public void kembalikanBuku(Scanner scanner) {
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/pbo", "root", "")) {
            boolean lanjutInput = true;
            while (lanjutInput) {
                System.out.print("Masukkan Nama Peminjam\t: ");
                String namaPeminjam = scanner.next();

                if (!isNamaPeminjamExists(connection, namaPeminjam)) {
                    System.out.println(
                            "Error: Nama Peminjam tidak ditemukan. Silakan masukkan nama peminjam yang valid.");
                    continue;
                }
                System.out.print("Masukkan Tanggal Pengembalian (Format: yyyy-MM-dd)\t: ");
                String tanggalPengembalianStr = scanner.next();

                Date tanggalPengembalian = null;
                try {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    tanggalPengembalian = dateFormat.parse(tanggalPengembalianStr);
                } catch (ParseException e) {
                    System.out.println("Error: Format tanggal tidak valid. Silakan coba lagi.");
                    continue;
                }

                updatePengembalianStatus(connection, namaPeminjam, tanggalPengembalian);

                System.out.println("Pengembalian berhasil dilakukan.");

                System.out.print("Apakah ingin melakukan pengembalian lagi? (y/n): ");
                String input = scanner.next();
                lanjutInput = input.equalsIgnoreCase("y");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updatePengembalianStatus(Connection connection, String namaPeminjam, Date tanggalPengembalian)
            throws SQLException {
        String query = "UPDATE peminjaman SET tanggalPengembalian = ?, status = ? WHERE namaPeminjam = ? AND status = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setDate(1, new java.sql.Date(tanggalPengembalian.getTime()));
            preparedStatement.setString(2, getStatusBasedOnReturnDate(connection, namaPeminjam, tanggalPengembalian));
            preparedStatement.setString(3, namaPeminjam);
            preparedStatement.setString(4, "Dipinjam");

            preparedStatement.executeUpdate();

            String bookQuery = "SELECT kodeBuku, jumlahBuku FROM peminjaman WHERE namaPeminjam = ?";
            try (PreparedStatement bookStatement = connection.prepareStatement(bookQuery)) {
                bookStatement.setString(1, namaPeminjam);
                try (ResultSet bookResultSet = bookStatement.executeQuery()) {
                    while (bookResultSet.next()) {
                        String kodeBuku = bookResultSet.getString("kodeBuku");
                        int jumlahDikembalikan = bookResultSet.getInt("jumlahBuku");

                        int currentStock = getStockFromDatabase(connection, kodeBuku);
                        updateStokInDatabase(connection, kodeBuku, currentStock + jumlahDikembalikan);
                    }
                }
            }
        }
    }

    private String getStatusBasedOnReturnDate(Connection connection, String namaPeminjam, Date tanggalPengembalian)
            throws SQLException {
        String query = "SELECT tanggalPinjam, tanggalKembali FROM peminjaman WHERE namaPeminjam = ? AND status = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, namaPeminjam);
            preparedStatement.setString(2, "Dipinjam");
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    Date tanggalKembali = resultSet.getDate("tanggalKembali");

                    if (tanggalPengembalian.before(tanggalKembali)) {
                        return "Dikembalikan tepat waktu";
                    } else if (tanggalPengembalian.after(tanggalKembali)) {
                        return "Dikembalikan terlambat";
                    }
                }
            }
        }
        return "";
    }

}
