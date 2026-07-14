# HazardScout PHP + MySQL server

## XAMPP installation
1. Start Apache and MySQL in XAMPP.
2. Copy this `server` folder to `C:\xampp\htdocs\hazardscout`.
3. Open phpMyAdmin and import `schema.sql`.
4. If your database credentials differ, edit `config/database.php`.
5. Verify the API in a browser:
   - `http://localhost/hazardscout/api/health.php`
   - `http://localhost/hazardscout/api/get_hazards.php`
6. Open the customized server dashboard:
   - `http://localhost/hazardscout/admin/`

## Android connection
- Android Emulator: `http://10.0.2.2/hazardscout/api/`
- Real phone on the same Wi-Fi: `http://YOUR-COMPUTER-IP/hazardscout/api/`
- Enter this URL inside **HazardScout > Settings > Server connection**, then tap **Test Connection**.

## Required records stored
The database stores the user name, server date/time, user agent, device information, app version, location name, latitude, longitude, hazard category, and hazard description.
