#ifndef _search_h
#define _search_h     

#define SOCKET_ERROR -1
#define	MAX_NUM_CAMERA	1000

typedef unsigned char BYTE;
typedef unsigned short WORD;

#ifndef bool
typedef int bool;
#endif

//extern "C" {


  enum
  {
	  _getBASIC, _setBASIC
  } cmd_list;

typedef struct BrowseHeader {
	BYTE Tag[4];   
	BYTE Ver[2];
	WORD Length;
	BYTE ProductID[4];
	WORD Command;
	WORD Result;
	WORD DataType;
	WORD DataLen;
} BrowseHeader;

typedef struct BrowseBasicPayload {
	BYTE Ver[2];
	BYTE MAC[6];
	BYTE DeviceName[16];
	BYTE Description[32];
	BYTE IP_Stat;			//  0: Dynamic		1: Static
	unsigned int IP_Addr;
	WORD WebPort;
	unsigned int NetMask;
	unsigned int Gateway;
} BrowseBasicPayload;

typedef struct ExtInformation {
      
        char     gAdminName[36];          //
        char     gAdminPasswd[16];        //
        char     gWLanEnable;            // Wireless Enable = 0x01;  Disable = 0x00
        char     gWlanOpMode;            // INFRASTRUCTURE=0; ADHOC=1;  Access Pont=2;
        char     gWlanAuthMethod ;        // Open System;Shared System
        char     WlanSsid[33] ;            // SSID
        short    WlanDomain;             // 1,2,......,14
        char     WlanChannel;            // auto=0, 1...14
        short    WlanEncryptMethod;       // None = 0; wep = 1 ; wpa = 2
        char     WlanDefKey;            // 1,2,3,4
        char     WlanWepKey1[27];       //
        char     WlanWepKey2[27];       //
        char     WlanWepKey3[27];       //
        char     WlanWepKey4[27];       //
        int      Dns1;
        int      Dns2;
        int      BonjouIP;               // Desire ip
        char     MegaMode;               // MegaMode
        char     Brand[32];              // Brand
        char     ExDeviceTitle[32];      // ExDeviceTitle ; PayloadVer=0x3,0x00
        char     DipsEnable;             //0x4,0x00 ���嚙踝蕭�秋�嚙質���IPS
        char     DIPS[10];               //DIPS嚙賜�瞍莎蕭嚙賭�9
} ExtInformation;

typedef struct BrowsePacket {
	struct BrowseHeader Header;
	struct BrowseBasicPayload Payload;
	struct ExtInformation ExtInfo;
	int redirectd_port;
} BrowsePacket;

typedef struct WifiInfo
	{
		// 0: auto, 1: wired
		int interfaceSelectMode;
		// 0: static, 1: DHCP
		int ipType;
		char ip[16];
		char gateway[16];
		char netMask[16];
		char dns[2][16];
		char mac[18];

		char ssid[33];
		char bssid[32];
		// 0: managed, 1: ad-hoc
		int type;
		// 0: auto
		int channel;
		// CGI: /cgi-bin/test.cgi?action=get_ap_info
		// 0: Off, 1: WEP, 2: WPA
		// CGI: /config.cgi?action=list&group=Network.Wireless
		// 0: none, 1: wep, 2: wpa-psk
		int securityMode;
		int signal;

		// 0: open, 1: shared
		int wepAuth;
		int wepKeyIndex;
		char wepKeys[4][27];
		
		// 0: WPAPSK, 1: WPA2PSK
		int wpaType;
		// 0: AES, 1: TKIP
		int wpaEncryption;
		char wpaKey[33];
	} WifiInfo;

typedef struct DeviceInfo {
	  int Used;
   BYTE Ver[2];
   BYTE MAC[6];
   //char DeviceName[16];
   char DeviceName[32];  //Paylod Ver = "30"
   char Description[32];
   unsigned int IP_Addr;
   unsigned int NetMask;
   unsigned int Gateway;
   char IPAddrStr[40];
   WORD WebPort;
   WORD ViewerNo;
   WORD Connected;
   WORD Local;
   int Fixed;
   int Number;
		
   BYTE ProductID[4];
   unsigned int DNS1;
   unsigned int DNS2;
   int NetStatus;
   BYTE Ver1[2];
   char     gWLanEnable;            // Wireless Enable = 0x01;  Disable = 0x00
   char     gWlanOpMode;            // INFRASTRUCTURE=0; ADHOC=1;  Access Pont=2;
   char     gWlanAuthMethod ;        // Open System;Shared System
   char     WlanSsid[33] ;            // SSID
   short    WlanDomain;             // 1,2,......,14
   char     WlanChannel;            // auto=0, 1...14
   short    WlanEncryptMethod;       // None = 0; wep = 1 ; wpa = 2
   char     WlanDefKey;            // 1,2,3,4
   char     WlanWepKey1[27];       //
   char     WlanWepKey2[27];       //
   char     WlanWepKey3[27];       //
   char     WlanWepKey4[27];       //
   unsigned int     WDns1;                  //
   unsigned int     WDns2;                  //
   int      BonjouIP;               // Desire ip
   char     Brand[32];               // Brand
   char     ExDeviceTitle[32];
   char     inSubnet;                  // -1: undefined, 0: not in subnet, 1: in subnet.
   char     DispEnable;
   char     DIPS[10];
   // [GP][MOD][111110]
   unsigned int     deviceType;  // 0: Video, 256: Router
   char     modelName[33];
	 char username[33];
	 char password[17];
	 WifiInfo wifiInfo;
  } DeviceInfo;




int init(int *, struct BrowsePacket *, unsigned short , char *);

//}
#endif

