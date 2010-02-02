package DVBViewer_Channels;

import java.nio.MappedByteBuffer;


public class Tuner {
	public static final int ENTRY_LENGTH = 52 ;
	private final Channels channels ;
	private byte  type ; 				// 0 = cable, 1 = satellite, 2 = terrestrial, 3 = atsc
	private byte  channelGroup ;		// 0 = Group A, 1 = Group B, 2 = Group C
	private byte  satModulationSystem ; // 0 = DVB-S, 1 = DVB-S2
										// SatModulationSystem is deprecated! Only set for compatibility
	private byte  flags ;				// Bit 0: 1 = encrypted channel
										// Bit 1: 1 = don't display EPG on What's Now Tab & in Timeline
										// Bit 2: 1 = channel broadcasts RDS data
										// Bit 3: 1 = channels is a video service (even if the Video PID is temporarily = 0)
										// Bit 4: 1 = channel is an audio service (even if the Audio PID is temporarily = 0)
										// Bit 5..7: reserved
	private int   frequency ;			// DWord: MHz for DVB-S, KHz for DVB-T/C and ATSC
	private int   smbolrate ; 			// DWord: DVB-S/C only
	private short lnbNB_LOF ;			// Word:  DVB-S only, local oscillator frequency of the LNB
	private short pmtPID ;				// Word
	private short reserved1 ;			// Word
	private byte  satModulation ;		// Bit 0..1: modulation. 0 = Auto, 1 = QPSK, 2 = 8PSK, 3 = 16QAM
										// Bit 2: modulation system. 0 = DVB-S, 1 = DVB-S2
										// Bit 3..4: roll-off. 0 = 0.35, 1 = 0.25, 2 = 0.20, 3 = reserved
										// Bit 5..6: spectral inversion. 0 = undefined, 1 = auto, 2 = normal, 3 = inverted
										// Bit 7: pilot symbols. 0 = off, 1 = on
										// Note: Bit 3..7 only apply to DVB-S2 and Hauppauge HVR 4000 / Nova S2 Plus
	private byte  avFormat ;			// Low Nibble (Bit 0..3): audio format
										//     0 = MPEG
										//     1 = AC3
										//     2..15 reserved
										// High Nibble (Bit 4..7): video format
										//     0 = MPEG2
										//     1 = H.264
										//     2..15 reserved
	private byte  fec ;	 				// 0 = Auto
										// 1 = 1/2
										// 2 = 2/3
										// 3 = 3/4
										// 4 = 5/6
										// 5 = 7/8
										// 6 = 8/9
										// 7 = 3/5
 										// 8 = 4/5
										// 9 = 9/10
	private byte  reserved2 ;			// must be 0
	private short reserved3 ;			// Word
	private byte  polarity ;			// DVB-S polarity
 										//     0 = horizontal
										//     1 = vertical
										//     2 = circular left
										//     3 = circular right
										// or DVB-C modulation
 										//     0 = Auto
										//     1 = 16QAM
										//     2 = 32QAM
										//     3 = 64QAM
										//     4 = 128QAM
										//     5 = 256 QAM
										// or DVB-T bandwidth
										//     0 = 6 MHz
										//     1 = 7 MHz
										//     2 = 8 MHz
	private byte  reserved4 ;			// must be 0
	private short reserved5 ;			// Word
	private byte  tone ; 				// Byte; //0 = off, 1 = 22 khz
	private byte  reserved6 ;			// must be 0
	private short diSEqCExt ;			// Word
										// DiSEqC Extension: OrbitPos, or other value
										// -> Positoner, GotoAngular, Command String (set to 0 if not required)
	private byte  diSEqC ;				// 0 = None
										// 1 = Pos A (mostly translated to PosA/OptA)
										// 2 = Pos B (mostly translated to PosB/OptA)
										// 3 = PosA/OptA
										// 4 = PosB/OptA
										// 5  =PosA/OptB
										// 6 = PosB/OptB
	private byte  reserved7 ;			// must be 0
	private short reserved8 ; 			// Word
	private short audioPID ; 			// Word;
	private short reserved9 ; 			// Word;
	private short videoPID ; 			// Word;
	private short transportStreamID ; 	// Word;
	private short teletextPID ; 		// Word;
	private short originalNetworkID ; 	// Word;
	private short serviceID ; 			// Word;
	private short pcrPID ; 				// Word;

	public Tuner( Channels channels )
	{
		this.channels = channels ;
	}
	public void read()
	{
		MappedByteBuffer buffer = channels.getMappedByteBuffer() ;
				
		this.type                = buffer.get() ;
		this.channelGroup        = buffer.get() ;
		this.satModulationSystem = buffer.get() ;
		this.flags               = buffer.get() ;
		this.frequency           = buffer.getInt() ;
		this.smbolrate           = buffer.getInt() ;
		this.lnbNB_LOF           = buffer.getShort() ;
		this.pmtPID              = buffer.getShort() ;
		this.reserved1           = buffer.getShort() ;
		this.satModulation       = buffer.get() ;
		this.avFormat            = buffer.get() ;
		this.fec                 = buffer.get() ;
		this.reserved2           = buffer.get() ;
		this.reserved3           = buffer.getShort() ;
		this.polarity            = buffer.get() ;
		this.reserved4           = buffer.get() ;
		this.reserved5           = buffer.getShort() ;
		this.tone                = buffer.get() ;
		this.reserved6           = buffer.get() ;
		this.diSEqCExt           = buffer.getShort() ;
		this.diSEqC              = buffer.get() ;
		this.reserved7           = buffer.get() ;
		this.reserved8           = buffer.getShort() ;
		this.audioPID            = buffer.getShort() ;
		this.reserved9           = buffer.getShort() ;
		this.videoPID            = buffer.getShort() ;
		this.transportStreamID   = buffer.getShort() ;
		this.teletextPID         = buffer.getShort() ;
		this.originalNetworkID   = buffer.getShort() ;
		this.serviceID           = buffer.getShort() ;
		this.pcrPID              = buffer.getShort() ;
	}
	public long getType()      { return (long)this.type     &   0xff ; } ;
	public long getAudioPID()  { return (long)this.audioPID & 0xffff ; } ;
	public long getServiceID() { return (long)this.serviceID & 0xffff ; } ;
	public boolean isVideo()   { return (this.flags & 0x08) != 0 ; } ;
}
