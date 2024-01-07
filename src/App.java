import java.util.Scanner;

public class App {
    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        BukuKomik bukuKomik = new BukuKomik(null, null, null, null);
        boolean lanjutInput = true;
        while (lanjutInput) {
            System.out.print("\033[H\033[2J"); // Membersihkan terminal
            System.out.flush(); // Mengeluarkan output agar layar bersih
            System.out.println("Menu:");
            System.out.println("1. Input Stok Buku");
            System.out.println("2. Tambah Stok Buku");
            System.out.println("3. Kurang Stok Buku");
            System.out.println("4. Lihat Stok Buku");
            System.out.println("5. Peminjaman Buku");
            System.out.println("6. Pengembalian Buku");
            System.out.println("7. Keluar");
            System.out.print("Pilih menu: ");
            int choice = scanner.nextInt();

            switch (choice) {
                case 1:
                    bukuKomik.tambahStok(scanner);
                    break;
                case 2:
                    bukuKomik.tambahStokBaru(scanner);
                    break;
                case 3:
                    bukuKomik.kurangStok(scanner);
                    break;
                case 4:
                    bukuKomik.displayStok();
                    break;
                case 5:
                    bukuKomik.pinjamBuku(scanner);
                    break;
                case 6:
                    bukuKomik.kembalikanBuku(scanner);
                    break;
                case 7:
                    System.out.println("Terima kasih!");
                    scanner.close();
                    System.exit(0);
                default:
                    System.out.println("Pilihan tidak valid.");
                    break;
            }

            System.out.print("Apakah ingin kembali ke menu? (y/n): ");
            String input = scanner.next();
            lanjutInput = input.equalsIgnoreCase("y");
        }

    }
}
