/*

	@author Jim

*/
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>
#include <sys/types.h>
#include <pthread.h> 

#include <sys/times.h>
#include <sys/select.h>
#include <linux/socket.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>

#include <sys/ioctl.h>
#include "ifaddrs.h"
#include <net/if.h>

#ifdef __linux__
//    Match Linux to FreeBSD
  #define AF_LINK AF_PACKET
#else
  #include <net/if_dl.h>
#endif

#include <jni.h>
#include <android/log.h>

//#include <mp4v2/mp4v2.h>
#include "search.h"
#include "pano_msg.h"
#include "angel.h"

//#include <live555.h>

enum
{
	PRD_TYPE_CAMERA = 0,
	PRD_TYPE_NVR
};

#define live555_noFFmpeg
#ifdef live555_noFFmpeg
#define FFMpegContext Live555Context
#endif

const char* BROADCAST_IP = "255.255.255.255";
const int BROADCAST_PORT = 4416;
const int BROADCAST_PORT_PNP = 42574;

static const int TMOUT_NO_VIDEO_FRAME = 20;

static const char* gAppSignatureMd5String = NULL;
static char gAppSignature[8192];
static int gValidAppSignature = 1;


const struct sockaddr_in* castToIP4(const struct sockaddr* addr) {
    if (addr == NULL) {
        return NULL;
    } else if (addr->sa_family == AF_INET) {
        // An IPv4 address
        return (const struct sockaddr_in*)(addr);
    } else {
        // Not an IPv4 address
        return NULL;
    }
}



int getBroadcastIP(char* ip)
{
	if(ip == NULL)
		return -1;


    // Head of the interface address linked list
    struct ifaddrs* ifap = NULL;
	//__android_log_print(ANDROID_LOG_ERROR, "zzz", "getSearchCamDid getBroadcastIP getifaddrs");
    int r = getifaddrs(&ifap);
	//__android_log_print(ANDROID_LOG_ERROR, "zzz", "getSearchCamDid getBroadcastIP getifaddrs r = %d", r);
    if (r != 0) {
        // Non-zero return code means an error
        __android_log_print(ANDROID_LOG_DEBUG, "zzz", "get_bip: return code = %d", r);
        return -1;
    }

    struct ifaddrs* current = ifap;

    if (current == NULL) {
        __android_log_print(ANDROID_LOG_DEBUG, "zzz", "get_bip: No interfaces found");
				return -1;
    }


		char found = 0;

    while (current != NULL) {
		//__android_log_print(ANDROID_LOG_ERROR, "zzz", "getSearchCamDid getBroadcastIP while");
        const struct sockaddr_in* interfaceAddress = castToIP4(current->ifa_addr);
        const struct sockaddr_in* broadcastAddress = castToIP4(current->ifa_dstaddr);
        const struct sockaddr_in* subnetMask       = castToIP4(current->ifa_netmask);

        //__android_log_print(ANDROID_LOG_DEBUG, "zzz", "get_bip: Interface %s", current->ifa_name);
        //if (current->ifa_addr != NULL) {
            //printf(" %s", levelToString(current->ifa_addr->sa_family));
        //}
        //__android_log_print(ANDROID_LOG_DEBUG, "zzz", "\nStatus    = %s\n", (current->ifa_flags & IFF_UP) ? "Online" : "Down");
				if(!(current->ifa_flags & IFF_UP) || interfaceAddress == NULL || broadcastAddress == NULL)
					goto NEXT_NET_IF;


				int addr = ntohl(interfaceAddress->sin_addr.s_addr);

				if(((addr >> 24) & 0xFF) == 127)
					goto NEXT_NET_IF;


				addr = ntohl(broadcastAddress->sin_addr.s_addr);

        sprintf(ip, "%d.%d.%d.%d", (addr >> 24) & 0xFF, (addr >> 16) & 0xFF, (addr >> 8) & 0xFF, addr & 0xFF);
				__android_log_print(ANDROID_LOG_DEBUG, "zzz", "get_bip: bip=%s", ip);

				found = 1;


NEXT_NET_IF:
        current = current->ifa_next;
    }
	//__android_log_print(ANDROID_LOG_ERROR, "zzz", "getSearchCamDid getBroadcastIP freeifaddrs");
    freeifaddrs(ifap);
    ifap = NULL;


		if(found)
			return 0;
		else
			return -201;
}


int initSearchPnpCamera(int* bswSocket, struct browse_packet* Packet, unsigned short cmd, char* localAddr)
{
    // clear();

    struct sockaddr_in local_addr_in;
    int ret, so_broadcast = 1;

    // fill structure
    // Add broadcast header data to buffer
    Packet->header.Tag[0] = 'A';
    Packet->header.Tag[1] = 'v';
    Packet->header.Tag[2] = 'C';
    Packet->header.Tag[3] = 'm';
    Packet->header.Ver[0] = '1';
    Packet->header.Length = sizeof(struct browse_packet);
    Packet->header.ProductID[0] = (char)0xFF;
    Packet->header.ProductID[1] = (char)0xFF;
    Packet->header.ProductID[2] = (char)0xFF;
    Packet->header.ProductID[3] = (char)0xFF;
    Packet->header.Command = cmd;
    Packet->header.Result = 0;
    // Packet->header.DataType = 0;
    Packet->header.DataLen = 0;

    // set send data Addr In
    if (localAddr == NULL) {
        local_addr_in.sin_addr.s_addr = htonl(INADDR_ANY);
        //__android_log_print(ANDROID_LOG_ERROR, "zzz", "null");
    } else {
        local_addr_in.sin_addr.s_addr = inet_addr(localAddr);
        //__android_log_print(ANDROID_LOG_ERROR, "zzz", "else");
    }
    local_addr_in.sin_family = AF_INET;
    local_addr_in.sin_port = htons(0);

    // *bswSocket = socket(AF_INET, SOCK_DGRAM,IPPROTO_IP);
    *bswSocket = socket(AF_INET, SOCK_DGRAM, IPPROTO_UDP);
    setsockopt(*bswSocket, SOL_SOCKET, SO_BROADCAST, (char*)&so_broadcast, sizeof(so_broadcast));
    ret = bind(*bswSocket, (struct sockaddr*)&local_addr_in, sizeof(local_addr_in));

    if (ret == -1)
        return -100;

    return ret;
}


void* searchKTVServer()
{
    int numItem = 0;

    // TODO: Add your control notification handler code here
    // SOCKET connectionSocket;
    int connectionSocket;
    struct sockaddr_in remote_addr_in;
    // unsigned int ip_addr;
    char dataBuffer[512];
    struct browse_packet* Packet;
    // int deviceIndex = 0;
    int ret, out_len;
    int i, iRetVal, bytesend;
    fd_set readfds;
    struct timeval timeout;
    time_t currtime, starttime;

    // char *dipID;
    char local_dips[20];
    char buffer[9];
	char DIPSStr[10];
    char current_string[] = "<devices>";
    int current_pos = 0;
    int dipsMatch = 0;
    bool showLog = 0;
	char temp[50];
    // dipID = malloc(20480);
    char* _target_dips;


    Packet = (struct browse_packet*)dataBuffer;
    initSearchPnpCamera(&connectionSocket, Packet, _getBASIC, 0);

    char bip[16] = {0};
    __android_log_print(ANDROID_LOG_DEBUG, "jim_localsearch","getBroadcastIP\n");
		int res = getBroadcastIP(bip);
    __android_log_print(ANDROID_LOG_DEBUG, "jim_localsearch","getBroadcastIP res = %d\n", res);
		if(res == 0) {
            __android_log_print(ANDROID_LOG_DEBUG, "jim_localsearch","getBroadcastIP ip = %s\n", bip);
            remote_addr_in.sin_addr.s_addr = inet_addr(bip);
        }
		else
			remote_addr_in.sin_addr.s_addr = inet_addr(BROADCAST_IP);

    remote_addr_in.sin_family = AF_INET;
    remote_addr_in.sin_port = htons(BROADCAST_PORT_PNP);

    // send browse packet for 3 times
    for (i = 0; i < 1; i++) {
        // get start get packet time
        time(&starttime);

        sendto(connectionSocket,
               dataBuffer,
               Packet->header.DataLen + 8,
               0,
               (struct sockaddr*)&remote_addr_in,
               sizeof(remote_addr_in));

        // receiver data
        // for(k=0;k<150;k++)
        while (1) {

            time(&currtime);

            if ((currtime - starttime) > 1)
                break;

            memset(dataBuffer, 0, 512);
            out_len = sizeof(remote_addr_in);

            timeout.tv_sec = 0;
            timeout.tv_usec = 100;

            FD_ZERO(&readfds);
            FD_SET(connectionSocket, &readfds);

            iRetVal = select(connectionSocket + 1, &readfds, NULL, NULL, &timeout);

            if (0 == iRetVal)
                continue;

            memset(dataBuffer, 0, 512);

            /*
             recvfrom(int socket, void *restrict buffer, size_t length, int flags,
             struct sockaddr *restrict address, socklen_t *restrict address_len);
             */

            ret = recvfrom(connectionSocket,
                           (void*)dataBuffer,
                           sizeof(dataBuffer),
                           0,
                           (struct sockaddr*)(socklen_t*)&remote_addr_in,
                           (socklen_t*)&out_len);

            if (ret == SOCKET_ERROR)
                continue;

            ntohl(Packet->payload.IP_Addr);

            // if(deviceMap[ip_addr] != NULL)
            //{
            //	continue;
            //}

            if (numItem >= MAX_NUM_CAMERA) {
                continue;
            }

            // struct sockaddr_in tmp_addr;

            // printf( "i=%d", i);

            memcpy(buffer, &dataBuffer[371], 50);

			int sum = atoi(buffer);

			if (showLog){
				printf("Length : %d \n",sum);
			}

			//__android_log_print(ANDROID_LOG_DEBUG, "jim_localsearch", "sum %d", sum);

			if(sum <= 0 && strlen(buffer) <= 0)
				continue;

			memcpy(&temp, &dataBuffer[371], strlen(buffer));
			__android_log_print(ANDROID_LOG_DEBUG, "jim_localsearch","%s\n", temp);


                numItem++;
            //}
        }
    }
    // printf( "numItem=%d", numItem);

  close(connectionSocket);

    // return dipID;

    return 0;
}


JNIEXPORT jstring Java_com_jim_ktv_ktvcontroller_MainActivity_searchKTVServer(JNIEnv* env, jclass jcls)
{
	char res_str[10240] = {0};
    __android_log_print(ANDROID_LOG_DEBUG, "jim_localsearch","searchKTVServer\n");
	searchKTVServer(res_str);
	return (*env)->NewStringUTF(env, res_str);
}
