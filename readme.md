# MySawit-AUTH

## Database Schema
https://app.eraser.io/workspace/UIuTCmf4r8F0oQXKQoRi

## Api Doc
https://docs.google.com/document/d/10HpIqgPsum000fG7jnK9GIEm32EbXZiKK6fvPFwZXpU/edit?usp=sharing

---

## Diagrams

Berikut adalah Component Diagram dan Code Diagrams untuk bagian autentikasi dan manajemen pengguna (MySawit-AUTH) 

### Component Diagram

![Component Diagram](assets/ComponentDiagram.png)

Diagram ini menunjukkan seluruh komponen dalam Auth Service beserta interaksinya dengan Frontend (Web/Mobile), Database (PostgreSQL Auth DB), dan External Service (Google OAuth). Diagram ini juga memvisualisasikan alur internal, seperti bagaimana controller mendelegasikan logika registrasi dan login ke service, hingga penyimpanan data pengguna dan relasi buruh-mandor.

### Code Diagrams

Diagram-diagram di bawah ini menunjukkan struktur class/interface di level kode dari modul Auth, dipisahkan berdasarkan fitur utama:

**1. Code Diagram 1 — Authentication Flow**: Menampilkan `AuthController`, interface `AuthService`, implementasinya `AuthServiceImpl`, `UserRepository`, `AuthProviderFactory`, dan `AuthTokenIssuer`. Diagram ini menunjukkan bagaimana proses registrasi dan login (baik password standar maupun via Google) dijalankan, divalidasi akunnya, hingga JWT diterbitkan.
![Code Diagram 1](assets/codeDiagramAuth.png)

**2. Code Diagram 2 — Assignment Flow**: Menampilkan `AssignmentController`, interface `AssignmentService`, implementasinya `AssignmentServiceImpl`, `BuruhMandorAssignmentRepository`, dan `UserRepository`. Diagram ini menunjukkan alur di mana Admin mengatur, menugaskan (assign), mengalihkan (reassign), serta mencabut penugasan (unassign) relasi antara Buruh dan Mandor.
![Code Diagram 2](assets/codeDiagramAssignment.png)

**3. Code Diagram 3 — User Management Flow**: Menampilkan `UserController`, interface `UserService`, implementasinya `UserServiceImpl`, `UserListAccessPolicy`, dan `UserRepository`. Diagram ini mendeskripsikan bagaimana profil pengguna diakses dan diedit (untuk pengguna sendiri), serta bagaimana Admin/Mandor memiliki akses tampilan list pengguna yang difilter dan divalidasi menurut *role-based access policy*.
![Code Diagram 3](assets/codeDiagramUser.png)

**4. Code Diagram 4 — Deleted User Tracking Flow**: Menampilkan `DeletedUserController`, interface `UserService`, implementasinya `UserServiceImpl`, dan `UserRepository`. Diagram ini memvisualisasikan bagaimana Admin dapat melacak dan melihat histori dan profil dari akun-akun yang telah dinonaktifkan (di-*soft-delete*).
![Code Diagram 4](assets/codeDiagramDeleted.png)