package bfs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import java.sql.*;
import java.util.Vector;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class BFS {

    private static Connection con;
    private static Statement st;

    public static void main(String[] args) throws SQLException, IOException {
        input();
        tampilkan();
//        tes();
    }

    private static void tes() throws SQLException {
        try {
            konek();
            st = con.createStatement();
            ResultSet rs = st.executeQuery("select url from bfs where akar_ke = '2'");
            Vector<String> url = new <String> Vector();
            while (rs.next()) {
                url.add(rs.getString("url"));
            }
            for (int j = 0; j < url.size(); j++) {
//                String url_anchor = "";
//                url_anchor = 
                System.out.println(url.get(j));
                //parsing(url_anchor, (akar + 1), 0);
            }
        }catch (SQLException e){
            System.err.println(e);
        }
    }
    

    private static void konek() throws SQLException {
        String dbHost = "jdbc:mysql://localhost:3306/uninform";
        String dbUser = "root";
        String dbPass = "";

        try {
            con = DriverManager.getConnection(dbHost, dbUser, dbPass);
            //System.out.println("SUKSES !!!");
            
        } catch (SQLException e) {
            System.err.println(e);
        }
    }

    private static void insert(String sql) {
        try {
            konek();
            st = con.createStatement();
            st.executeUpdate(sql);
        } catch (SQLException e) {
            System.err.println(e);
        }
    }

    private static int select(String url_link) throws SQLException {
        
        String sql = "select count(*) from bfs where url = '" + url_link + "';";
        //System.out.println(sql);
        int count = 0;
        try {
            konek();
            st = con.createStatement();
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                count = rs.getInt("count(*)");//cek apakah ada link serupa
            }
        } catch (SQLException e) {
            System.err.println(e);
        }
        return count;
    }

    private static void input() throws IOException, SQLException {
        BufferedReader x = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("Masukan website : ");
        String alamat = x.readLine();
        System.out.print("Masukan jumlah tingkat akar : ");
        int akar = Integer.parseInt(x.readLine());
        int pertama = 1;
        buat_akar(alamat, akar, pertama);
    }

    private static void buat_akar(String alamat, int akar, int pertama) throws IOException, SQLException {
        akar = akar - 1;
        for (int i = 1; i <= akar; i++) {
            if (pertama == 1) {
                parsing(alamat, i, pertama);
                pertama = 0;
            } else {
                try {
                    st = con.createStatement();
                    ResultSet rs = st.executeQuery("select url from bfs where akar_ke = " + akar);
                    Vector<String> url = new <String> Vector();
                    while (rs.next()) {
                        url.add(rs.getString("url"));
                    }
                    for (int j = 0; j < url.size(); j++) {
                        String url_anchor = "";
                        url_anchor = url.get(j);
                        parsing(url_anchor, (akar + 1), 0);
                    }
                } catch (SQLException e) {
                    System.err.println(e);
                }
//                
            }
        }
    }

    private static void parsing(String url, int akar, int pertama) throws IOException, SQLException {
        String sql = "insert into bfs(nama_url,url,induk,jumlah_anak,anak_ke,akar_ke,konten) ";
        
        if (pertama == 1) {
            url = "http://www." + url;
            Document doc = Jsoup.connect(url).get();
            Elements anchors = doc.select("a");
            String sql_value = "";
            int jumlah_anak = anchors.size();
            Elements text = doc.select("body");
            String teks = text.text();
            teks = teks.replaceAll("[(-+.^:,'|&?!)]", "").replaceAll("yang", "").replaceAll("dengan", "").replaceAll("dan", "").replaceAll("dari", "");
            sql_value = "values('UINSA Surabaya','" + url + "',''," + jumlah_anak + ",0," + akar + ",'"+ teks +"')";
            sql = sql + sql_value;
            //System.out.println(sql);
            insert(sql);
            akar = akar + 1;
            pertama = 0;
        }

        Document doc = Jsoup.connect(url).get();
        Elements anchors = doc.select("a");
        int urutan_anak = 1;

        //parsing link utama-------------------------------
        for (Element anchor : anchors) {
            sql = "insert into bfs(nama_url,url,induk,jumlah_anak,anak_ke,akar_ke,konten) ";
            String sql_value = "";
            String nama_url = "";
            String url_anchor = "";
            String induk = "";
            int jumlah_anak = 0;
            int anak_ke;
            nama_url = anchor.text().toString(); //Ambil nama link
            nama_url = nama_url.replaceAll("'", "''");
            url_anchor = anchor.attr("abs:href"); //Ambil href link = link_ambil
            url_anchor = url_anchor.replaceAll("'", "''");
            induk = url;
            anak_ke = urutan_anak;
            try {
                jumlah_anak = jumlah_anak(url_anchor);
            } catch (IOException e) {

            }
            System.out.println(url_anchor);//tes print--
            String teks = "";
            try{
                Document doc1 = Jsoup.connect(url_anchor).get();
                Elements text = doc1.select("body");
                teks = text.text();
            } catch (IOException e){
                
            }
            
            teks = teks.replaceAll("[(-+.^:,'|&?!)]", "").replaceAll("yang", "").replaceAll("dengan", "").replaceAll("dan", "").replaceAll("dari", "");
            sql_value = "values('" + nama_url + "','" + url_anchor + "','" + induk + "'," + jumlah_anak + "," + anak_ke + "," + akar + ",'" + teks + "');";
            sql = sql + sql_value;
            int cek;
            cek = select(url_anchor);
            if (cek == 0) {
                insert(sql);
            }
            urutan_anak++;
        }
        //-------------------------------------------------------------------------------------
    }

    private static int jumlah_anak(String url_anchor) throws IOException {
        int jumlah_anak = 0;
        if (url_anchor.equals("")) {
            //System.out.println("Jumlah anak = kosong");
        } else {
            Document doc1 = Jsoup.connect(url_anchor).get();//Cek isi link dari link_ambil
            Elements anchors1 = doc1.select("a");

            jumlah_anak = anchors1.size();
            //System.out.println("Jumlah anak = " + jumlah_anak);
        }
        return jumlah_anak;
    }

    public static void tampilkan() throws SQLException {
        konek();
        try {
            st = con.createStatement();
            ResultSet rs = st.executeQuery("select count(akar_ke) from bfs group by akar_ke");
            int jumlah_akar = 0;
            while (rs.next()) {
                jumlah_akar = rs.getInt("count(akar_ke)");
            }
            for (int i = 1; i <= jumlah_akar; i++) {
                try {
                    st = con.createStatement();
                    ResultSet rs1 = st.executeQuery("select * from bfs where akar_ke = " + i);
                    if (i == 1) {
                        System.out.println("---------------------------");
                        while (rs1.next()) {
                            String nama_url = rs1.getString("url");
                            System.out.println("----------" + nama_url + "--------");
                        }
                    } else {
                        st = con.createStatement();
                        ResultSet rs2 = st.executeQuery("select * from bfs where akar_ke = " + i);
                        while (rs2.next()) {
                            String induk = rs2.getString("induk");
                            String url = rs2.getString("url");
                            System.out.print("Asal : " + induk);
                            System.out.println("--> " + url);
                        }
                        System.out.println("---------------------------");
                    }

                } catch (SQLException e) {
                    System.err.println(e);
                }
                System.out.println("------------Akar ke" + (i + 1) + "-------------");
            }
        } catch (SQLException e) {
            System.err.println(e);
        }
    }

}
