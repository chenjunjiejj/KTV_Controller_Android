#ifndef __IP_FINDER__
#define __IP_FINDER__

/****************************************
 * IP CAM finder server side daemon
 * All the structure below are modified
 * from client side browser.
 *					Jerry Chen
 ****************************************/

struct browse_header
{
    char Tag[4];
    char Ver[2];
    short Length;
    char ProductID[4];
    short Command;
    short Result;
    short SPort;
    short DataLen;
} __attribute((packed));

typedef struct browse_header packet_header;
struct browse_payload
{
    char Ver[2];
    char MAC[6];
    char DeviceName[16];
    char Description[32];
    char IP_Stat; //  0: Dynamic		1: Static
    int IP_Addr;
    short WebPort;
    int NetMask;
    int Gateway;
} __attribute((packed));
typedef struct browse_payload packet_payload;

struct ExtInform
{
    char gAdminName[36];
    char gAdminPasswd[16];
    char gWLanEnable;     // Enable = 0x01;  Disable = 0x00
    char gWlanOpMode;     // INFRASTRUCTURE=0; ADHOC=1;  Access Pont=2;
    char gWlanAuthMethod; // Open System;Shared System
    char WlanSsid[33];
    short WlanDomain;        // 1,2,......,14
    char WlanChannel;        // Auto=0;
    short WlanEncryptMethod; // None=-1; 64Bits=0; 128 Bits=1
    char WlanDefKey;         // 1,2,3,4
    char WlanWepKey1[27];
    char WlanWepKey2[27];
    char WlanWepKey3[27];
    char WlanWepKey4[27];
    int Dns1;
    int Dns2;
    int BonjouIP;       // Bonjou ip address
    char Megamode;      // Mega mode=0x01, else 0x00
    char Brand[32];     // Brand name
    char ModelName[32]; // Brand name
    char support_DIPS;
    char DIPS[10];
    char https[4];
    char httpsenable[4];
    char SystemBoaGroupPolicy[8];
    short httpsport;
} __attribute((packed));
typedef struct ExtInform packet_extern;

struct browse_packet
{
    packet_header header;
    packet_payload payload;
    packet_extern exinfo;
    int redirectd_port;
    char ProdNbr[16];
    char SummazySettingID;
} __attribute((packed));

typedef struct browse_packet IPfinder_packet;

typedef enum {
    RTN_OK,
    RTN_NG
} RTN_TYPE;

RTN_TYPE IPCAM_Daemon(void);

struct modifyip_packet
{
    char Tag[4];             // "McVa"
    char MAC[6];             // MAC address
    char Login_Username[32]; // login name
    char Login_Password[16]; // login password
    char New_Username[32];   // login name
    char New_Password[16];   // login password
    int change_userinfo;     // 0: Do not modify user info    1: Modify user info
    int dhcp;                // 0: Dynamic    1: Static
    int IP_Addr;             // Desire ip address
    int NetMask;             // Desire netmask address
    int Gateway;             // Desire gateway address
    int dns1;                // Desire dns1 address
    int dns2;                // Desire dns2 address
} __attribute((packed));
typedef struct modifyip_packet Modify_Packet;

#endif
