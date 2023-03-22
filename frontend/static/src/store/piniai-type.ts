export type SocketStore = {
  isConnected: boolean;
  message: string;
  reconnectError: boolean;
  heartBeatInterval: number;
  heartBeatTimer: number;
};
export type socketType = {
  $connect: () => void;
};
