# 📱 **ColorControl Universe - Android TV Remote Control**

<div align="center">

![ColorControl Logo](https://img.shields.io/badge/ColorControl-Universe-blue?style=for-the-badge&logo=android)
![Version](https://img.shields.io/badge/Version-1.0.0-green?style=for-the-badge)
![Android](https://img.shields.io/badge/Android-5.0%2B-brightgreen?style=for-the-badge&logo=android)

**แอปพลิเคชันควบคุมทีวีอัจฉริยะที่รองรับการเชื่อมต่อผ่าน WiFi, Bluetooth และ USB**

</div>

## 🎯 **คุณสมบัติหลัก**

### 🌐 **การเชื่อมต่อแบบสากล**
- **WiFi Network** - เชื่อมต่อผ่านเครือข่ายท้องถิ่น
- **Bluetooth** - เชื่อมต่อแบบไร้สายกับทีวีที่รองรับ
- **USB OTG** - เชื่อมต่อผ่านสาย USB (สำหรับทีวีรุ่นพิเศษ)
- **Auto-Discovery** - ค้นหาทีวีในเครือข่ายอัตโนมัติ

### 📺 **รองรับทีวีหลายแบรนด์**
- ✅ **LG** (WebOS)
- ✅ **Samsung** (Tizen)  
- ✅ **Sony** (BRAVIA)
- ✅ **Panasonic** (VIERA)
- ✅ **และอื่นๆ** (Generic TV Support)

### 🎮 **การควบคุมที่ครบครัน**
- ปุ่มเปิด/ปิดทีวี
- ควบคุมเสียง (ขึ้น/ลง/ปิดเสียง)
- เปลี่ยนช่อง (ขึ้น/ลง)
- การนำทาง (ขึ้น/ลง/ซ้าย/ขวา/ตกลง)
- ปุ่มฟังก์ชัน (หน้าหลัก/ย้อนกลับ/เมนู)
- ป้อนหมายเลขช่องโดยตรง
- ปลุกทีวีจากโหมดสแตนบาย

### 🎨 **ประสบการณ์ผู้ใช้**
- **UI สไตล์จักรวาล** - การออกแบบที่ทันสมัยด้วย Cosmic Design
- **การตอบสนองที่ลื่นไหล** - อนิเมชันและเอฟเฟกต์ที่สมจริง
- **โหมดมืด/สว่าง** - ปรับเปลี่ยนตามความชอบ
- **การตอบรับแบบสัมผัส** - การสั่นและเสียงตอบรับ
- **รองรับหลายภาษา** - ภาษาไทยและภาษาอังกฤษ

## 🚀 **การติดตั้งและใช้งาน**

### **ข้อกำหนดระบบ**
- Android 5.0 (API 21) ขึ้นไป
- พื้นที่ว่างอย่างน้อย 50 MB
- การอนุญาตการเข้าถึงเครือข่ายและบลูทูธ

### **ขั้นตอนการติดตั้ง**

1. **ดาวน์โหลด APK**
   ```bash
   # ดาวน์โหลดไฟล์ APK ล่าสุด
   wget https://github.com/maassoft/ColorControl-Android/releases/latest/download/app-release.apk
   ```

2. **ติดตั้งบนอุปกรณ์**
   ```bash
   # ติดตั้งผ่าน ADB
   adb install app-release.apk
   
   # หรือติดตั้ง manually บนอุปกรณ์
   ```

3. **เปิดใช้งานการติดตั้งจากแหล่งที่ไม่รู้จัก**
   - ไปที่ Settings → Security → Unknown Sources → อนุญาต

### **การตั้งค่าเริ่มต้น**

1. **เปิดแอปครั้งแรก**
   - ระบบจะแสดงหน้าตั้งค่าเริ่มต้น (Setup Wizard)

2. **ให้สิทธิ์การเข้าถึง**
   - อนุญาตการเข้าถึง WiFi และ Bluetooth
   - อนุญาตการเข้าถึงตำแหน่ง (สำหรับ Bluetooth)

3. **เลือกวิธีการเชื่อมต่อ**
   - **อัตโนมัติ**: ค้นหาทีวีอัตโนมัติ
   - **WiFi**: ระบุ IP Address ของทีวี
   - **Bluetooth**: ค้นหาและจับคู่อุปกรณ์
   - **USB**: เชื่อมต่อผ่านสาย USB

## 📱 **การใช้งาน**

### **การเชื่อมต่อกับทีวี**

#### 1. **เชื่อมต่อแบบอัตโนมัติ**
```java
// ระบบจะค้นหาทีวีในเครือข่ายอัตโนมัติ
TvDiscovery discovery = new TvDiscovery(context);
List<TvDevice> devices = discovery.discoverTvs();
```

#### 2. **เชื่อมต่อแบบ Manual**
```java
// ระบุข้อมูลทีวีด้วยตนเอง
TvDevice tv = new TvDevice("LG TV", "192.168.1.100", "LG");
tv.setMacAddress("AA:BB:CC:DD:EE:FF");

UniversalConnectionManager manager = new UniversalConnectionManager(context);
manager.connect(tv, ConnectionType.WIFI);
```

### **การควบคุมทีวี**

```java
// ส่งคำสั่งควบคุม
tvControlService.sendPowerCommand();      // เปิด/ปิด
tvControlService.sendVolumeUp();          // เสียงขึ้น
tvControlService.sendVolumeDown();        // เสียงลง
tvControlService.sendMute();              // ปิดเสียง
tvControlService.sendChannelUp();         // ช่องขึ้น
tvControlService.sendChannelDown();       // ช่องลง
tvControlService.sendHome();              // หน้าหลัก
tvControlService.sendBack();              // ย้อนกลับ
```

### **คุณสมบัติพิเศษ**

#### 🔋 **Wake-on-LAN**
```java
// ปลุกทีวีจากโหมดสแตนบาย
WakeOnLan.sendWakeOnLan(macAddress, ipAddress);
```

#### 🔄 **Auto-Reconnect**
```java
// เชื่อมต่อใหม่อัตโนมัติเมื่อการเชื่อมต่อขาด
connectionManager.setAutoReconnect(true);
```

#### 📊 **Signal Monitoring**
```java
// ตรวจสอบคุณภาพสัญญาณ
int signalStrength = NetworkUtils.getNetworkSignalStrength(context);
String quality = NetworkUtils.getSignalQuality(signalStrength);
```

## 🏗️ **โครงสร้างโปรเจกต์**

```
ColorControl-Android/
├── app/
│   ├── src/main/
│   │   ├── java/com/maassoft/colorcontrol/
│   │   │   ├── activities/           # หน้าจอต่างๆ
│   │   │   ├── controllers/          # ควบคุมทีวีแต่ละแบรนด์
│   │   │   ├── network/              # การจัดการเครือข่าย
│   │   │   ├── bluetooth/            # การเชื่อมต่อบลูทูธ
│   │   │   ├── usb/                  # การเชื่อมต่อ USB
│   │   │   ├── services/             # Background Services
│   │   │   ├── utils/                # Utilities
│   │   │   └── widgets/              # Custom UI Components
│   │   ├── res/                      # Resources
│   │   └── AndroidManifest.xml
│   └── build.gradle
├── docs/                             # เอกสารประกอบ
└── scripts/                          # Scripts สำหรับ Build
```

## 🔧 **การพัฒนา**

### **Requirements**
- Android Studio Arctic Fox หรือใหม่กว่า
- Android SDK 34
- Java 8 หรือ Kotlin
- Gradle 8.0+

### **การ Build โปรเจกต์**

```bash
# Clone repository
git clone https://github.com/maassoft/ColorControl-Android.git
cd ColorControl-Android

# Build Debug APK
./gradlew assembleDebug

# Build Release APK
./gradlew assembleRelease

# Run tests
./gradlew test
```

### **การกำหนดค่าเพิ่มเติม**

#### **เพิ่มทีวีแบรนด์ใหม่**
```java
public class NewBrandController implements TvController {
    @Override
    public boolean connect(String ip) {
        // Implementation
    }
    
    @Override
    public boolean sendCommand(String command) {
        // Implementation
    }
}
```

#### **เพิ่ม Protocol ใหม่**
```java
public class NewProtocol implements TvCommandProtocol {
    @Override
    public byte[] encodeCommand(String commandType) {
        // Command encoding
    }
}
```

## 🐛 **การแก้ไขปัญหา**

### **ปัญหาทั่วไป**

1. **ไม่พบทีวีในเครือข่าย**
   - ตรวจสอบว่าทีวีและมือถืออยู่ใน WiFi วงเดียวกัน
   - ตรวจสอบการตั้งค่า Mobile App บนทีวี
   - ลองใช้การค้นหาแบบ Manual ด้วย IP Address

2. **การเชื่อมต่อ Bluetooth ล้มเหลว**
   - ตรวจสอบการจับคู่อุปกรณ์ก่อน
   - เปิดการมองเห็นของทีวี
   - ตรวจสอบสิทธิ์การเข้าถึงตำแหน่ง

3. **คำสั่งไม่ทำงาน**
   - ตรวจสอบว่า TV เปิดอยู่
   - ตรวจสอบการตั้งค่า Remote Control บนทีวี
   - ลองใช้พอร์ตอื่น (8080, 8001, 10000)

### **Logging และ Debugging**

```java
// เปิด Debug Mode
Log.d("ColorControl", "Connection state: " + connectionState);
Log.d("ColorControl", "Command sent: " + command);

// ตรวจสอบการเชื่อมต่อเครือข่าย
boolean available = NetworkUtils.isNetworkAvailable(context);
String ssid = NetworkUtils.getCurrentSsid(context);
```

## 📊 **Performance**

### **การใช้งานทรัพยากร**
- **RAM Usage**: ~50-80 MB
- **Battery Impact**: ต่ำ (ใช้เฉพาะเมื่อควบคุม)
- **Network Usage**: ต่ำ (เฉพาะเมื่อส่งคำสั่ง)

### **Optimization**
- Connection pooling สำหรับเครือข่าย
- Background service management
- Memory leak prevention
- Efficient Bluetooth connection handling

## 🤝 **การมีส่วนร่วม**

เรายินดีรับการมีส่วนร่วมจากชุมชน! 

### **วิธีการมีส่วนร่วม**
1. Fork repository
2. สร้าง feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit การเปลี่ยนแปลง (`git commit -m 'Add some AmazingFeature'`)
4. Push ไปยัง branch (`git push origin feature/AmazingFeature`)
5. Open Pull Request

### **รายงานปัญหา**
กรุณาใช้ [GitHub Issues](https://github.com/maassoft/ColorControl-Android/issues) สำหรับการรายงานปัญหา

## 📄 **ใบอนุญาต**

โปรเจกต์นี้ใช้ใบอนุญาต MIT - ดูไฟล์ [LICENSE](LICENSE) สำหรับรายละเอียด

## 🙏 **การให้เครดิต**

### **ผู้พัฒนา**
- **Maassoft Team** - การพัฒนาเริ่มต้น
- **Contributors** - ผู้มีส่วนร่วมจากชุมชน

### **เทคโนโลยีที่ใช้**
- [OkHttp](https://square.github.io/okhttp/) - HTTP Client
- [Material Components](https://material.io/develop/android) - UI Components
- [Gson](https://github.com/google/gson) - JSON Processing

## 📞 **การติดต่อ**

- **GitHub**: [maassoft/ColorControl-Android](https://github.com/maassoft/ColorControl-Android)
- **Email**: support@maassoft.com
- **Website**: [https://maassoft.com](https://maassoft.com)

## 🎉 **ข้อความสุดท้าย**

> "ColorControl Universe ไม่ใช่แค่รีโมทควบคุมทีวี แต่คือประสบการณ์การควบคุมทีวีแบบใหม่ที่ไร้ขีดจำกัด 🌌"

---

<div align="center">

**พร้อมควบคุมจักรวาลทีวีของคุณแล้ว! 🚀📺**

</div>
