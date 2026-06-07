/*
 * Buy Property Page - Real Estate Management System
 * FEATURES:
 *   1. Free-text location field + dropdown city suggestions
 *   2. Multiple image gallery in detail popup (Next / Prev arrows)
 *   3. Seller ONLY can Update & Delete (username + password verified)
 *   4. Fully responsive / resizable window (WrapLayout)
 *   5. Opens MAXIMIZED on all screens
 *
 * HOW TO CONNECT:
 *   In home.java -> btnbuyActionPerformed(), write:
 *       new buyproperty().setVisible(true);
 *       this.dispose();
 */

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.sql.*;
import javax.imageio.ImageIO;

public class buyproperty extends javax.swing.JFrame {

    // ── Colours (match your project) ──────────────────────────────────
    private static final Color NAVY       = new Color(0, 0, 51);
    private static final Color CYAN_ACC   = new Color(0, 204, 204);
    private static final Color WHITE      = Color.WHITE;
    private static final Color CARD_BG    = new Color(245, 248, 252);
    private static final Color CARD_HOVER = new Color(220, 240, 255);
    private static final Color BORDER_CLR = new Color(200, 220, 240);
    private static final Color PRICE_CLR  = new Color(0, 140, 140);
    private static final Color BTN_TEAL   = new Color(0, 153, 153);

    // ── Pakistan cities for autocomplete suggestion list ──────────────
    private static final String[] PK_CITIES = {
        "Karachi","Lahore","Islamabad","Rawalpindi","Faisalabad",
        "Multan","Peshawar","Quetta","Sialkot","Gujranwala",
        "Hyderabad","Bahawalpur","Sargodha","Sukkur","Larkana",
        "Mardan","Abbottabad","DHA Karachi","DHA Lahore","Gulshan-e-Iqbal",
        "Clifton","PECHS","North Nazimabad","Johar Town","Model Town",
        "Bahria Town Karachi","Bahria Town Lahore","Bahria Town Islamabad",
        "F-10 Islamabad","G-11 Islamabad","Saddar","Defence","Gulberg",
        "Cantt","Nazimabad","Korangi","Landhi","Malir","Shah Faisal"
    };

    // ── Filter components ─────────────────────────────────────────────
    private JTextField        txtSearch;
    private JTextField        txtLocation;
    private JList<String>     locationSuggest;
    private DefaultListModel<String> suggestModel;
    private JPopupMenu        suggestPopup;
    private JComboBox<String> cmbType;
    private JTextField        txtMinPrice, txtMaxPrice;

    // ── Cards panel ───────────────────────────────────────────────────
    private JPanel pnlCards;
    private JLabel lblResultCount;

    // ─────────────────────────────────────────────────────────────────
    // CONSTRUCTOR
    // ─────────────────────────────────────────────────────────────────
    public buyproperty() {
        setTitle("Buy Property – Real Estate");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(820, 620));

        // ── MAXIMIZED on startup (works on all screen sizes) ──────────
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        getContentPane().setLayout(new BorderLayout());

        JPanel north = new JPanel(new BorderLayout());
        north.add(buildHeader(),  BorderLayout.NORTH);
        north.add(buildFilter(),  BorderLayout.SOUTH);
        add(north,          BorderLayout.NORTH);
        add(buildResults(), BorderLayout.CENTER);

        setVisible(true);
        loadProperties("", "", 0, Double.MAX_VALUE, "");
    }

    // ══════════════════════════════════════════════════════════════════
    //  1. HEADER
    // ══════════════════════════════════════════════════════════════════
    private JPanel buildHeader() {
        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setBackground(NAVY);
        hdr.setPreferredSize(new Dimension(0, 130));

        // top row: Back | Title | Logout
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(NAVY);
        top.setBorder(new EmptyBorder(8, 14, 0, 14));

        JButton bBack = navBtn("← Back");
        bBack.addActionListener(e -> { new home().setVisible(true); dispose(); });
        top.add(bBack, BorderLayout.WEST);

        JLabel title = new JLabel("Real Estate", SwingConstants.CENTER);
        title.setFont(new Font("Verdana", Font.BOLD, 52));
        title.setForeground(WHITE);
        top.add(title, BorderLayout.CENTER);

        JButton bLogout = navBtn("Logout");
        bLogout.addActionListener(e -> { new login().setVisible(true); dispose(); });
        top.add(bLogout, BorderLayout.EAST);
        hdr.add(top, BorderLayout.NORTH);

        // subtitle
        JLabel sub = new JLabel(
            "Smart Property Deals Start Here", SwingConstants.CENTER);
        sub.setFont(new Font("Verdana", Font.BOLD, 26));
        sub.setForeground(CYAN_ACC);
        sub.setBorder(new EmptyBorder(2, 0, 4, 0));
        hdr.add(sub, BorderLayout.CENTER);

        // BUY / SELL nav buttons
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 40, 4));
        btnRow.setBackground(NAVY);

        JButton bBuy = navBtn("BUY PROPERTY");
        bBuy.setForeground(WHITE);
        bBuy.setBorder(BorderFactory.createLineBorder(WHITE, 2));

        JButton bSell = navBtn("SELL PROPERTY");
        bSell.setForeground(WHITE);
        bSell.setBorder(BorderFactory.createLineBorder(WHITE, 2));
        bSell.addActionListener(e -> {
            new oldsellproperty().setVisible(true); dispose();
        });

        btnRow.add(bBuy); btnRow.add(bSell);
        hdr.add(btnRow, BorderLayout.SOUTH);
        return hdr;
    }

    // ══════════════════════════════════════════════════════════════════
    //  2. FILTER PANEL
    // ══════════════════════════════════════════════════════════════════
    private JPanel buildFilter() {
        JPanel pnl = new JPanel(new WrapLayout(FlowLayout.LEFT, 12, 8));
        pnl.setBackground(new Color(232, 240, 250));
        pnl.setBorder(new CompoundBorder(
            new MatteBorder(0, 0, 2, 0, CYAN_ACC),
            new EmptyBorder(8, 14, 10, 14)));

        JLabel lbl = new JLabel("🔍  Search & Filter Properties");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lbl.setForeground(NAVY);
        pnl.add(stretch(lbl));

        // Search by title
        txtSearch = fld(175);
        pnl.add(grp("Search Title:", txtSearch));

        // FREE TEXT location + live suggestions
        txtLocation = fld(185);
        txtLocation.setToolTipText(
            "Type any city, area or street: DHA, Gulshan, Clifton …");

        suggestModel  = new DefaultListModel<>();
        locationSuggest = new JList<>(suggestModel);
        locationSuggest.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        locationSuggest.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        locationSuggest.setBackground(WHITE);

        suggestPopup = new JPopupMenu();
        suggestPopup.setBorder(new LineBorder(BORDER_CLR));
        JScrollPane sp = new JScrollPane(locationSuggest);
        sp.setPreferredSize(new Dimension(185, 120));
        sp.setBorder(null);
        suggestPopup.add(sp);

        txtLocation.getDocument().addDocumentListener(
            new javax.swing.event.DocumentListener() {
                public void insertUpdate (javax.swing.event.DocumentEvent e) { suggest(); }
                public void removeUpdate (javax.swing.event.DocumentEvent e) { suggest(); }
                public void changedUpdate(javax.swing.event.DocumentEvent e) { suggest(); }
            });

        locationSuggest.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                String sel = locationSuggest.getSelectedValue();
                if (sel != null) {
                    txtLocation.setText(sel);
                    suggestPopup.setVisible(false);
                }
            }
        });

        pnl.add(grp("Location (type any city/area):", txtLocation));

        // Type dropdown
        cmbType = new JComboBox<>(new String[]{"All Types","Sell","Rent"});
        cmbType.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cmbType.setPreferredSize(new Dimension(130, 30));
        pnl.add(grp("Type:", cmbType));

        // Price range
        txtMinPrice = fld(115); txtMinPrice.setText("0");
        txtMaxPrice = fld(115); txtMaxPrice.setText("999999999");
        pnl.add(grp("Min Price (Rs):", txtMinPrice));
        pnl.add(grp("Max Price (Rs):", txtMaxPrice));

        // Apply / Reset
        JButton bApply = filterBtn("Apply Filter", NAVY, WHITE);
        bApply.setBorder(BorderFactory.createLineBorder(CYAN_ACC, 2));
        bApply.addActionListener(e -> applyFilter());
        pnl.add(bApply);

        JButton bReset = filterBtn("Reset", new Color(168, 168, 178), NAVY);
        bReset.addActionListener(e -> resetFilter());
        pnl.add(bReset);

        return pnl;
    }

    private void suggest() {
        String txt = txtLocation.getText().trim();
        suggestModel.clear();
        if (txt.isEmpty()) { suggestPopup.setVisible(false); return; }
        for (String city : PK_CITIES)
            if (city.toLowerCase().contains(txt.toLowerCase()))
                suggestModel.addElement(city);
        if (suggestModel.isEmpty()) { suggestPopup.setVisible(false); return; }
        suggestPopup.show(txtLocation, 0, txtLocation.getHeight());
        txtLocation.requestFocus();
    }

    // ══════════════════════════════════════════════════════════════════
    //  3. RESULTS AREA
    // ══════════════════════════════════════════════════════════════════
    private JPanel buildResults() {
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(new Color(236, 243, 252));

        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 6));
        bar.setBackground(new Color(220, 230, 246));
        bar.setBorder(new MatteBorder(0, 0, 1, 0, BORDER_CLR));
        lblResultCount = new JLabel("Loading properties…");
        lblResultCount.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblResultCount.setForeground(NAVY);
        bar.add(lblResultCount);
        wrap.add(bar, BorderLayout.NORTH);

        pnlCards = new JPanel(new WrapLayout(FlowLayout.LEFT, 16, 16));
        pnlCards.setBackground(new Color(236, 243, 252));
        pnlCards.setBorder(new EmptyBorder(10, 10, 10, 10));

        JScrollPane sc = new JScrollPane(pnlCards);
        sc.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        sc.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        sc.getVerticalScrollBar().setUnitIncrement(20);
        sc.setBorder(null);
        wrap.add(sc, BorderLayout.CENTER);
        return wrap;
    }

    // ══════════════════════════════════════════════════════════════════
    //  LOAD PROPERTIES FROM DB
    // ══════════════════════════════════════════════════════════════════
        // ══════════════════════════════════════════════════════════════════
    // LOAD PROPERTIES FROM DB - Updated
    // ══════════════════════════════════════════════════════════════════
    private void loadProperties(String location, String type,
                                 double minP, double maxP, String search) {
        pnlCards.removeAll();
        try {
            Connection con = DBConnection.connect();
            if (con == null) {
                lblResultCount.setText("❌ DB connection failed!");
                pnlCards.revalidate(); pnlCards.repaint(); return;
            }
            
            StringBuilder sql = new StringBuilder(
                "SELECT id, title, location, price, image_path, type, contact, " +
                "description, emergency_contact, seller_username FROM properties WHERE 1=1");
            
            if (!location.isEmpty()) sql.append(" AND location LIKE ?");
            if (!type.isEmpty() && !type.equals("All Types"))
                sql.append(" AND type=?");
            if (!search.isEmpty()) sql.append(" AND title LIKE ?");
            sql.append(" AND price BETWEEN ? AND ? ORDER BY id DESC");
            
            PreparedStatement pst = con.prepareStatement(sql.toString());
            int i = 1;
            if (!location.isEmpty())
                pst.setString(i++, "%" + location + "%");
            if (!type.isEmpty() && !type.equals("All Types"))
                pst.setString(i++, type);
            if (!search.isEmpty())
                pst.setString(i++, "%" + search + "%");
            pst.setDouble(i++, minP);
            pst.setDouble(i, maxP);
            
            ResultSet rs = pst.executeQuery();
            int cnt = 0;
            while (rs.next()) {
                cnt++;
                pnlCards.add(makeCard(
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getString("location"),
                    rs.getDouble("price"),
                    rs.getString("image_path"),
                    rs.getString("type"),
                    rs.getString("contact"),
                    rs.getString("description"),
                    rs.getString("emergency_contact"),
                    rs.getString("Seller_username")   // Capital S   // ← Added
                ));
            }
            lblResultCount.setText(cnt == 0 ? "0 properties found"
                : cnt + " propert" + (cnt == 1 ? "y" : "ies") + " found");
            
            if (cnt == 0) {
                JLabel none = new JLabel(
                    " 😔 No properties found. Try a different search.");
                none.setFont(new Font("Segoe UI", Font.BOLD, 18));
                none.setForeground(new Color(150, 155, 165));
                pnlCards.add(none);
            }
            con.close();
        } catch (Exception ex) {
            lblResultCount.setText("❌ Error: " + ex.getMessage());
            ex.printStackTrace();
        }
        pnlCards.revalidate(); pnlCards.repaint();
    }
    
    // ══════════════════════════════════════════════════════════════════
    //  PROPERTY CARD
    // ══════════════════════════════════════════════════════════════════
        // ══════════════════════════════════════════════════════════════════
    // PROPERTY CARD - Updated
    // ══════════════════════════════════════════════════════════════════
    private JPanel makeCard(int id, String title, String location,
                             double price, String imagePaths, String type,
                             String contact, String desc, String emergency, String sellerUsername) {
        
        JPanel card = new JPanel(new BorderLayout());
        card.setPreferredSize(new Dimension(300, 400));
        card.setBackground(CARD_BG);
        card.setBorder(new CompoundBorder(
            new LineBorder(BORDER_CLR, 1, true),
            new EmptyBorder(0, 0, 6, 0)));

        card.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                card.setBackground(CARD_HOVER);
                card.setBorder(new CompoundBorder(
                    new LineBorder(CYAN_ACC, 2, true),
                    new EmptyBorder(0, 0, 6, 0)));
            }
            public void mouseExited(MouseEvent e) {
                card.setBackground(CARD_BG);
                card.setBorder(new CompoundBorder(
                    new LineBorder(BORDER_CLR, 1, true),
                    new EmptyBorder(0, 0, 6, 0)));
            }
        });

        // Image thumbnail (keep your original image code)
        JLabel imgLbl = new JLabel("", SwingConstants.CENTER);
        imgLbl.setPreferredSize(new Dimension(300, 170));
        imgLbl.setOpaque(true);
        imgLbl.setBackground(new Color(205, 218, 236));
        String[] paths = splitPaths(imagePaths);
        loadThumb(imgLbl, paths.length > 0 ? paths[0] : null, 300, 170);
        if (paths.length > 1) {
            imgLbl.setText("<html>**📷 " + paths.length + "**</html>");
            imgLbl.setHorizontalTextPosition(SwingConstants.RIGHT);
            imgLbl.setVerticalTextPosition(SwingConstants.BOTTOM);
        }
        card.add(imgLbl, BorderLayout.NORTH);

        // Info section (keep your original)
        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setBackground(CARD_BG);
        info.setBorder(new EmptyBorder(8,12,4,12));
        String t = (type != null && !type.isEmpty()) ? type : "Property";
        JLabel badge = new JLabel(" " + t.toUpperCase() + " ");
        badge.setFont(new Font("Segoe UI", Font.BOLD, 10));
        badge.setForeground(WHITE);
        badge.setBackground(t.equalsIgnoreCase("Rent") ? new Color(175,85,0) : NAVY);
        badge.setOpaque(true);
        badge.setBorder(new EmptyBorder(2,5,2,5));
        info.add(badge); info.add(Box.createVerticalStrut(6));
        info.add(boldLabel(nvl(title), 14, NAVY));
        info.add(Box.createVerticalStrut(4));
        info.add(plainLabel("📍 " + nvl(location), 13, new Color(70,88,115)));
        info.add(Box.createVerticalStrut(5));
        info.add(boldLabel("Rs " + String.format("%,.0f", price), 16, PRICE_CLR));
        info.add(Box.createVerticalStrut(4));
        if (contact != null && !contact.isEmpty())
            info.add(plainLabel("📞 " + contact, 12, new Color(95,108,132)));
        card.add(info, BorderLayout.CENTER);

        // ── BUTTONS ──────────────────────────────────────────────────
        JPanel btns = new JPanel(new GridLayout(1, 3, 4, 0));
        btns.setBackground(CARD_BG);
        btns.setBorder(new EmptyBorder(5,8,2,8));

        JButton bView = cBtn("👁 View & Buy", BTN_TEAL, WHITE);
        bView.addActionListener(e ->
            openDetailDialog(id, title, location, price, paths, type, contact, desc, emergency));

        JButton bEdit = cBtn("✏ Update", new Color(0,100,185), WHITE);
        bEdit.addActionListener(e -> {
            if (verifySeller(id, "update")) {
                openUpdateDialog(id, title, location, price, imagePaths, type, contact, desc, emergency);
            }
        });

        JButton bDel = cBtn("🗑 Delete", new Color(188,30,30), WHITE);
        bDel.addActionListener(e -> {
            if (verifySeller(id, "delete")) {
                deleteAd(id, title);
            }
        });

        btns.add(bView); btns.add(bEdit); btns.add(bDel);

        JPanel bWrap = new JPanel(new BorderLayout());
        bWrap.setBackground(CARD_BG);
        bWrap.setBorder(new EmptyBorder(4,8,2,8));
        bWrap.add(btns);
        card.add(bWrap, BorderLayout.SOUTH);
        return card;
    }

  // ══════════════════════════════════════════════════════════════════
// UPDATED SELLER VERIFICATION (Owner Check)
// ══════════════════════════════════════════════════════════════════
private boolean verifySeller(int propertyId, String action) {
    JPanel panel = new JPanel(new GridBagLayout());
    panel.setBackground(WHITE);
    panel.setBorder(new EmptyBorder(10, 10, 10, 10));
    GridBagConstraints gc = new GridBagConstraints();
    gc.insets = new Insets(6, 5, 6, 5);
    gc.fill = GridBagConstraints.HORIZONTAL;

    gc.gridx = 0; gc.gridy = 0; gc.gridwidth = 2;
    JLabel info = new JLabel(
        "<html><b>🔐 Owner Verification Required</b><br>" +
        "<font color='gray'>Only the seller who posted this ad can " + action + " it.</font></html>");
    info.setFont(new Font("Segoe UI", Font.PLAIN, 13));
    panel.add(info, gc);

    gc.gridwidth = 1; gc.gridy = 1; gc.gridx = 0;
    panel.add(new JLabel("Username:"), gc);
    gc.gridx = 1;
    JTextField uField = new JTextField(16);
    panel.add(uField, gc);

    gc.gridy = 2; gc.gridx = 0;
    panel.add(new JLabel("Password:"), gc);
    gc.gridx = 1;
    JPasswordField pField = new JPasswordField(16);
    panel.add(pField, gc);

    int result = JOptionPane.showConfirmDialog(this, panel,
        "🔐 Seller Verification", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

    if (result != JOptionPane.OK_OPTION) return false;

    String username = uField.getText().trim();
    String password = new String(pField.getPassword()).trim();

    if (username.isEmpty() || password.isEmpty()) {
        JOptionPane.showMessageDialog(this,
            "Please enter username and password!", "Missing Info", JOptionPane.WARNING_MESSAGE);
        return false;
    }

    try {
        Connection con = DBConnection.connect();
        
        PreparedStatement pst = con.prepareStatement(
            "SELECT * FROM properties p " +
            "JOIN users u ON p.Seller_username = u.username " +
            "WHERE p.id = ? AND p.Seller_username = ? AND u.password = ?");

        pst.setInt(1, propertyId);
        pst.setString(2, username);
        pst.setString(3, password);

        ResultSet rs = pst.executeQuery();

        if (rs.next()) {
            con.close();
            return true; // ✅ Correct owner
        } else {
            con.close();
            JOptionPane.showMessageDialog(this,
                "❌ Access Denied!\nYou are not the owner of this ad.\n\n" +
                "Use the same username you entered while posting the property.",
                "Access Denied", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    } catch (Exception ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(this, "Database Error!", "Error", JOptionPane.ERROR_MESSAGE);
        return false;
    }
}
    // ══════════════════════════════════════════════════════════════════
    //  DETAIL DIALOG — multi-image gallery with Prev / Next
    // ══════════════════════════════════════════════════════════════════
    private void openDetailDialog(int id, String title, String location,
                                   double price, String[] paths,
                                   String type, String contact,
                                   String desc, String emergency) {

        JDialog dlg = new JDialog(this, "Property Details", true);
        dlg.setSize(820, 660);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());

        // header
        JPanel hdr = new JPanel(new FlowLayout(FlowLayout.LEFT, 18, 12));
        hdr.setBackground(NAVY);
        JLabel ht = new JLabel("🏠  Property Details");
        ht.setFont(new Font("Segoe UI", Font.BOLD, 22));
        ht.setForeground(WHITE);
        hdr.add(ht);
        dlg.add(hdr, BorderLayout.NORTH);

        // body
        JPanel body = new JPanel(new GridBagLayout());
        body.setBackground(WHITE);
        body.setBorder(new EmptyBorder(14, 18, 8, 18));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(5, 5, 5, 5);
        gc.anchor = GridBagConstraints.NORTHWEST;

        // ── Image gallery ─────────────────────────────────────────────
        int[] imgIdx = {0};

        JLabel imgDisplay = new JLabel("", SwingConstants.CENTER);
        imgDisplay.setPreferredSize(new Dimension(320, 255));
        imgDisplay.setOpaque(true);
        imgDisplay.setBackground(new Color(205, 218, 236));
        imgDisplay.setBorder(new LineBorder(BORDER_CLR));
        loadThumb(imgDisplay, paths.length > 0 ? paths[0] : null, 320, 255);

        JLabel imgCounter = new JLabel(
            paths.length == 0 ? "No images" : "1 / " + paths.length,
            SwingConstants.CENTER);
        imgCounter.setFont(new Font("Segoe UI", Font.BOLD, 12));
        imgCounter.setForeground(NAVY);

        JButton bPrev = new JButton("◀ Prev");
        JButton bNext = new JButton("Next ▶");
        for (JButton b : new JButton[]{bPrev, bNext}) {
            b.setBackground(NAVY); b.setForeground(WHITE);
            b.setFont(new Font("Segoe UI", Font.BOLD, 12));
            b.setFocusPainted(false); b.setBorderPainted(false);
            b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        }
        bPrev.setEnabled(false);
        bNext.setEnabled(paths.length > 1);

        bPrev.addActionListener(e -> {
            if (imgIdx[0] > 0) {
                imgIdx[0]--;
                loadThumb(imgDisplay, paths[imgIdx[0]], 320, 255);
                imgCounter.setText((imgIdx[0]+1) + " / " + paths.length);
                bPrev.setEnabled(imgIdx[0] > 0);
                bNext.setEnabled(imgIdx[0] < paths.length - 1);
            }
        });
        bNext.addActionListener(e -> {
            if (imgIdx[0] < paths.length - 1) {
                imgIdx[0]++;
                loadThumb(imgDisplay, paths[imgIdx[0]], 320, 255);
                imgCounter.setText((imgIdx[0]+1) + " / " + paths.length);
                bPrev.setEnabled(imgIdx[0] > 0);
                bNext.setEnabled(imgIdx[0] < paths.length - 1);
            }
        });

        JPanel gallery = new JPanel(new BorderLayout(4, 4));
        gallery.setBackground(WHITE);
        gallery.add(imgDisplay, BorderLayout.CENTER);

        JPanel navRow = new JPanel(new BorderLayout(6, 0));
        navRow.setBackground(WHITE);
        navRow.add(bPrev,       BorderLayout.WEST);
        navRow.add(imgCounter,  BorderLayout.CENTER);
        navRow.add(bNext,       BorderLayout.EAST);
        gallery.add(navRow, BorderLayout.SOUTH);

        gc.gridx=0; gc.gridy=0; gc.gridheight=9;
        gc.fill=GridBagConstraints.NONE;
        body.add(gallery, gc);
        gc.gridheight=1;
        gc.fill=GridBagConstraints.HORIZONTAL;

        // ── Info rows ─────────────────────────────────────────────────
        String[][] rows = {
            {"ID",        "#" + id},
            {"Title",     nvl(title)},
            {"Location",  "📍 " + nvl(location)},
            {"Price",     "Rs " + String.format("%,.0f", price)},
            {"Type",      nvl(type)},
            {"Contact",   "📞 " + nvl(contact)},
            {"Emergency", "📞 " + nvl(emergency)}
        };
        for (int r = 0; r < rows.length; r++) {
            gc.gridx=1; gc.gridy=r; gc.weightx=0;
            JLabel k = new JLabel(rows[r][0] + ":");
            k.setFont(new Font("Segoe UI", Font.BOLD, 13));
            k.setForeground(NAVY);
            k.setPreferredSize(new Dimension(100, 28));
            body.add(k, gc);

            gc.gridx=2; gc.weightx=1.0;
            JLabel v = new JLabel(rows[r][1]);
            v.setFont(new Font("Segoe UI",
                r == 3 ? Font.BOLD : Font.PLAIN,
                r == 3 ? 15 : 13));
            v.setForeground(r == 3 ? PRICE_CLR : Color.DARK_GRAY);
            body.add(v, gc);
        }

        // description
        gc.gridx=1; gc.gridy=rows.length; gc.weightx=0;
        gc.fill=GridBagConstraints.NONE;
        body.add(boldLabel("Description:", 13, NAVY), gc);

        gc.gridx=1; gc.gridy=rows.length + 1;
        gc.gridwidth=2; gc.fill=GridBagConstraints.BOTH;
        gc.weightx=1.0; gc.weighty=1.0;
        JTextArea ta = new JTextArea(nvl(desc));
        ta.setEditable(false); ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        ta.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        ta.setBackground(new Color(245, 248, 252));
        JScrollPane dsp = new JScrollPane(ta);
        dsp.setPreferredSize(new Dimension(380, 85));
        dsp.setBorder(new LineBorder(BORDER_CLR));
        body.add(dsp, gc);
        dlg.add(body, BorderLayout.CENTER);

        // footer
        JPanel foot = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 12));
        foot.setBackground(new Color(236, 244, 252));
        foot.setBorder(new MatteBorder(1, 0, 0, 0, BORDER_CLR));

        JButton bBuy = dlgBtn("✅  Confirm Purchase",
            new Color(0, 128, 90), WHITE, 215);
        bBuy.addActionListener(e ->
            confirmPurchase(id, title, price, contact, dlg));

        JButton bCall = dlgBtn("📞  Contact Seller", NAVY, WHITE, 185);
        bCall.addActionListener(e ->
            JOptionPane.showMessageDialog(dlg,
                "Seller   : " + nvl(contact) +
                "\nEmergency: " + nvl(emergency) +
                "\n\nCall to finalise the deal.",
                "Contact Info", JOptionPane.INFORMATION_MESSAGE));

        JButton bClose = dlgBtn("Close",
            new Color(165, 165, 175), NAVY, 110);
        bClose.addActionListener(e -> dlg.dispose());

        foot.add(bBuy); foot.add(bCall); foot.add(bClose);
        dlg.add(foot, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    // ══════════════════════════════════════════════════════════════════
    //  UPDATE DIALOG
    // ══════════════════════════════════════════════════════════════════
    private void openUpdateDialog(int id, String title, String location,
                                   double price, String imagePaths, String type,
                                   String contact, String desc, String emergency) {

        JDialog dlg = new JDialog(this, "Update Property Ad", true);
        dlg.setSize(590, 650);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());

        // header
        JPanel hdr = new JPanel(new FlowLayout(FlowLayout.LEFT, 18, 12));
        hdr.setBackground(NAVY);
        JLabel ht = new JLabel("✏  Update Ad  —  #" + id);
        ht.setFont(new Font("Segoe UI", Font.BOLD, 20));
        ht.setForeground(WHITE);
        hdr.add(ht);
        dlg.add(hdr, BorderLayout.NORTH);

        // form fields pre-filled
        JTextField fTitle     = formFld(nvl(title));
        JTextField fLocation  = formFld(nvl(location));
        JTextField fPrice     = formFld(String.valueOf((long) price));
        JComboBox<String> fType = new JComboBox<>(new String[]{"Sell","Rent"});
        fType.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        if (type != null) fType.setSelectedItem(type);
        JTextField fContact   = formFld(nvl(contact));
        JTextField fEmergency = formFld(nvl(emergency));
        JTextField fImgPath   = formFld(nvl(imagePaths));
        JTextArea  fDesc      = new JTextArea(nvl(desc), 4, 20);
        fDesc.setLineWrap(true);
        fDesc.setWrapStyleWord(true);
        fDesc.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        Object[][] rows = {
            {"Title:",                        fTitle},
            {"Location (city/area/street):",  fLocation},
            {"Price (Rs):",                   fPrice},
            {"Type:",                         fType},
            {"Contact:",                      fContact},
            {"Emergency Contact:",            fEmergency},
            {"Image Path(s):",                fImgPath}
        };

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(WHITE);
        form.setBorder(new EmptyBorder(14, 28, 10, 28));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(7, 5, 7, 5);
        gc.anchor = GridBagConstraints.WEST;
        gc.fill   = GridBagConstraints.HORIZONTAL;

        for (int r = 0; r < rows.length; r++) {
            gc.gridx=0; gc.gridy=r; gc.weightx=0;
            JLabel lbl = new JLabel((String) rows[r][0]);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
            lbl.setForeground(NAVY);
            lbl.setPreferredSize(new Dimension(220, 28));
            form.add(lbl, gc);
            gc.gridx=1; gc.weightx=1.0;
            form.add((Component) rows[r][1], gc);
        }

        // Browse button for multiple images
        gc.gridx=1; gc.gridy=rows.length; gc.weightx=1.0;
        gc.fill=GridBagConstraints.NONE;
        JButton bBrowse = new JButton("📂 Browse Images (multiple allowed)");
        bBrowse.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        bBrowse.addActionListener(e -> {
            JFileChooser ch = new JFileChooser();
            ch.setMultiSelectionEnabled(true);
            if (ch.showOpenDialog(dlg) == JFileChooser.APPROVE_OPTION) {
                StringBuilder sb = new StringBuilder();
                for (File f : ch.getSelectedFiles())
                    sb.append(f.getAbsolutePath()).append(";");
                fImgPath.setText(sb.toString());
            }
        });
        form.add(bBrowse, gc);

        // Description
        gc.gridx=0; gc.gridy=rows.length + 1; gc.weightx=0;
        gc.fill=GridBagConstraints.NONE;
        JLabel dLbl = new JLabel("Description:");
        dLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        dLbl.setForeground(NAVY);
        form.add(dLbl, gc);

        gc.gridx=0; gc.gridy=rows.length + 2;
        gc.gridwidth=2; gc.weightx=1.0; gc.weighty=1.0;
        gc.fill=GridBagConstraints.BOTH;
        JScrollPane ds = new JScrollPane(fDesc);
        ds.setBorder(new LineBorder(new Color(180, 200, 220)));
        ds.setPreferredSize(new Dimension(0, 90));
        form.add(ds, gc);

        dlg.add(new JScrollPane(form), BorderLayout.CENTER);

        // footer
        JPanel foot = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 12));
        foot.setBackground(new Color(236, 244, 252));
        foot.setBorder(new MatteBorder(1, 0, 0, 0, BORDER_CLR));

        JButton bSave = dlgBtn("💾  Save Changes",
            new Color(0, 128, 78), WHITE, 180);
        bSave.addActionListener(e -> {
            double np;
            try {
                np = Double.parseDouble(fPrice.getText().trim());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dlg,
                    "Please enter a valid price (numbers only).",
                    "Invalid Price", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                Connection con = DBConnection.connect();
                PreparedStatement pst = con.prepareStatement(
                    "UPDATE properties SET title=?,location=?,price=?,type=?," +
                    "contact=?,emergency_contact=?,image_path=?,description=?" +
                    " WHERE id=?");
                pst.setString(1, fTitle.getText().trim());
                pst.setString(2, fLocation.getText().trim());
                pst.setDouble(3, np);
                pst.setString(4, fType.getSelectedItem().toString());
                pst.setString(5, fContact.getText().trim());
                pst.setString(6, fEmergency.getText().trim());
                pst.setString(7, fImgPath.getText().trim());
                pst.setString(8, fDesc.getText().trim());
                pst.setInt(9,    id);
                pst.executeUpdate();
                con.close();
                JOptionPane.showMessageDialog(dlg,
                    "✅ Property updated successfully!",
                    "Updated", JOptionPane.INFORMATION_MESSAGE);
                dlg.dispose();
                applyFilter();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg,
                    "❌ Update failed: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });

        JButton bCancel = dlgBtn("Cancel",
            new Color(165, 165, 175), NAVY, 110);
        bCancel.addActionListener(e -> dlg.dispose());
        foot.add(bSave); foot.add(bCancel);
        dlg.add(foot, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    // ══════════════════════════════════════════════════════════════════
    //  DELETE AD
    // ══════════════════════════════════════════════════════════════════
    private void deleteAd(int id, String title) {
        int ok = JOptionPane.showConfirmDialog(this,
            "⚠  Delete this property ad?\n\n" +
            "  Title : " + nvl(title) + "\n  ID    : " + id +
            "\n\nThis CANNOT be undone!",
            "Confirm Delete", JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);

        if (ok == JOptionPane.YES_OPTION) {
            try {
                Connection con = DBConnection.connect();
                PreparedStatement pst = con.prepareStatement(
                    "DELETE FROM properties WHERE id=?");
                pst.setInt(1, id);
                pst.executeUpdate();
                con.close();
                JOptionPane.showMessageDialog(this,
                    "🗑  Ad deleted successfully.",
                    "Deleted", JOptionPane.INFORMATION_MESSAGE);
                applyFilter();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                    "❌ " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════
    //  CONFIRM PURCHASE
    // ══════════════════════════════════════════════════════════════════
    private void confirmPurchase(int id, String title, double price,
                                  String contact, JDialog parent) {
        int ok = JOptionPane.showConfirmDialog(parent,
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
            "   PURCHASE CONFIRMATION\n" +
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n" +
            "  Property : " + nvl(title) + "\n" +
            "  Price    : Rs " + String.format("%,.0f", price) + "\n" +
            "  Seller   : " + nvl(contact) + "\n\n" +
            "Proceed with purchase request?",
            "Confirm", JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);

        if (ok == JOptionPane.YES_OPTION) {
            try {
                Connection con = DBConnection.connect();
                PreparedStatement pst = con.prepareStatement(
                    "UPDATE properties SET status='Sold' WHERE id=?");
                pst.setInt(1, id);
                pst.executeUpdate();
                con.close();
            } catch (Exception ex) { ex.printStackTrace(); }

            JOptionPane.showMessageDialog(parent,
                "🎉 Purchase Request Submitted!\n\n" +
                "Property : " + nvl(title) + "\n" +
                "Seller will contact you at: " + nvl(contact),
                "Success", JOptionPane.INFORMATION_MESSAGE);
            parent.dispose();
            applyFilter();
        }
    }

    // ══════════════════════════════════════════════════════════════════
    //  FILTER HELPERS
    // ══════════════════════════════════════════════════════════════════
    private void applyFilter() {
        String loc  = txtLocation.getText().trim();
        String type = cmbType.getSelectedItem().toString();
        String srch = txtSearch.getText().trim();
        double min = 0, max = Double.MAX_VALUE;
        try { min = Double.parseDouble(txtMinPrice.getText().trim()); } catch (Exception x) {}
        try { max = Double.parseDouble(txtMaxPrice.getText().trim()); } catch (Exception x) {}
        loadProperties(loc, type, min, max, srch);
    }

    private void resetFilter() {
        txtLocation.setText(""); txtSearch.setText("");
        cmbType.setSelectedIndex(0);
        txtMinPrice.setText("0"); txtMaxPrice.setText("999999999");
        loadProperties("", "", 0, Double.MAX_VALUE, "");
    }

    // ══════════════════════════════════════════════════════════════════
    //  UTILITY METHODS
    // ══════════════════════════════════════════════════════════════════
    private String[] splitPaths(String raw) {
        if (raw == null || raw.trim().isEmpty()) return new String[0];
        String[] parts = raw.split(";");
        java.util.List<String> list = new java.util.ArrayList<>();
        for (String p : parts) {
            String t = p.trim();
            if (!t.isEmpty()) list.add(t);
        }
        return list.toArray(new String[0]);
    }

    private void loadThumb(JLabel lbl, String path, int w, int h) {
        lbl.setIcon(null);
        if (path != null && !path.trim().isEmpty()) {
            try {
                File f = new File(path.trim());
                if (f.exists()) {
                    BufferedImage bi = ImageIO.read(f);
                    lbl.setIcon(new ImageIcon(
                        bi.getScaledInstance(w, h, Image.SCALE_SMOOTH)));
                    lbl.setText("");
                    return;
                }
            } catch (Exception ex) { /* fall through */ }
        }
        lbl.setText("<html><center><font size='5' color='#8899AA'>🏠" +
            "<br><font size='2'>No Image</font></font></center></html>");
    }

    private JButton navBtn(String t) {
        JButton b = new JButton(t);
        b.setBackground(NAVY); b.setForeground(CYAN_ACC);
        b.setFont(new Font("Arial", Font.BOLD, 16));
        b.setBorderPainted(false); b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JTextField fld(int w) {
        JTextField tf = new JTextField();
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tf.setPreferredSize(new Dimension(w, 30));
        tf.setBorder(new CompoundBorder(
            new LineBorder(new Color(175, 198, 220)),
            new EmptyBorder(2, 6, 2, 6)));
        return tf;
    }

    private JPanel grp(String label, Component comp) {
        JPanel p = new JPanel(new BorderLayout(4, 2));
        p.setBackground(new Color(232, 240, 250));
        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.BOLD, 11));
        l.setForeground(NAVY);
        p.add(l, BorderLayout.NORTH);
        p.add(comp, BorderLayout.CENTER);
        return p;
    }

    private JPanel stretch(Component c) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        p.setBackground(new Color(232, 240, 250));
        p.setPreferredSize(new Dimension(10000, 26));
        p.add(c);
        return p;
    }

    private JButton filterBtn(String t, Color bg, Color fg) {
        JButton b = new JButton(t);
        b.setBackground(bg); b.setForeground(fg);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setFocusPainted(false); b.setBorderPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(130, 32));
        return b;
    }

    private JButton cBtn(String t, Color bg, Color fg) {
        JButton b = new JButton(t);
        b.setBackground(bg); b.setForeground(fg);
        b.setFont(new Font("Segoe UI", Font.BOLD, 10));
        b.setFocusPainted(false); b.setBorderPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JButton dlgBtn(String t, Color bg, Color fg, int w) {
        JButton b = new JButton(t);
        b.setBackground(bg); b.setForeground(fg);
        b.setFont(new Font("Segoe UI", Font.BOLD, 14));
        b.setFocusPainted(false); b.setBorderPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(w, 40));
        return b;
    }

    private JTextField formFld(String v) {
        JTextField tf = new JTextField(v);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tf.setBorder(new CompoundBorder(
            new LineBorder(new Color(175, 198, 220)),
            new EmptyBorder(3, 6, 3, 6)));
        return tf;
    }

    private JLabel boldLabel(String t, int sz, Color c) {
        JLabel l = new JLabel(t);
        l.setFont(new Font("Segoe UI", Font.BOLD, sz));
        l.setForeground(c);
        return l;
    }

    private JLabel plainLabel(String t, int sz, Color c) {
        JLabel l = new JLabel(t);
        l.setFont(new Font("Segoe UI", Font.PLAIN, sz));
        l.setForeground(c);
        return l;
    }

    private String nvl(String s) {
        return (s == null || s.trim().isEmpty()) ? "N/A" : s.trim();
    }

    // ══════════════════════════════════════════════════════════════════
    //  WRAP LAYOUT
    // ══════════════════════════════════════════════════════════════════
    static class WrapLayout extends FlowLayout {
        WrapLayout(int a, int h, int v) { super(a, h, v); }

        @Override
        public Dimension preferredLayoutSize(Container t) { return ls(t, true); }

        @Override
        public Dimension minimumLayoutSize(Container t) {
            Dimension d = ls(t, false);
            d.width -= (getHgap() + 1);
            return d;
        }

        private Dimension ls(Container t, boolean pref) {
            synchronized (t.getTreeLock()) {
                int tw = t.getSize().width;
                if (tw == 0) tw = Integer.MAX_VALUE;
                Insets in = t.getInsets();
                int maxW = tw - in.left - in.right - getHgap() * 2;
                Dimension dim = new Dimension(0, 0);
                int rw = 0, rh = 0;
                for (int i = 0; i < t.getComponentCount(); i++) {
                    Component m = t.getComponent(i);
                    if (!m.isVisible()) continue;
                    Dimension d = pref
                        ? m.getPreferredSize() : m.getMinimumSize();
                    if (rw + d.width > maxW) {
                        dim.width = Math.max(dim.width, rw);
                        if (dim.height > 0) dim.height += getVgap();
                        dim.height += rh; rw = 0; rh = 0;
                    }
                    if (rw != 0) rw += getHgap();
                    rw += d.width;
                    rh = Math.max(rh, d.height);
                }
                dim.width = Math.max(dim.width, rw);
                if (dim.height > 0) dim.height += getVgap();
                dim.height += rh;
                dim.width  += in.left + in.right + getHgap() * 2;
                dim.height += in.top + in.bottom + getVgap() * 2;
                return dim;
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════
    //  MAIN
    // ══════════════════════════════════════════════════════════════════
    public static void main(String[] args) {
        try {
            for (UIManager.LookAndFeelInfo info :
                    UIManager.getInstalledLookAndFeels())
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName()); break;
                }
        } catch (Exception e) {}
        java.awt.EventQueue.invokeLater(() -> new buyproperty().setVisible(true));
    }
}
