#!/usr/local/bin/perl
# Dont make fun of my path

use Socket;

$src_host = $ARGV[0]; # The source IP/Hostname
$src_port = $ARGV[1]; # The Source Port
$dst_host = $ARGV[2]; # The Destination IP/Hostname
$dst_port = $ARGV[3]; # The Destination Port.

 if(!defined $src_host or !defined $src_port or !defined $dst_host or !defined $dst_port) {
   print "Usage: $0 <source host> <source port> <dest host> <dest port>\n";
   exit;
 } 
 else {
  main();
 }
 
sub main {
 my $src_host = (gethostbyname($src_host))[4];
 my $dst_host = (gethostbyname($dst_host))[4];

 socket(RAW, AF_INET, SOCK_RAW, 255) || die $!;
 setsockopt(RAW, 0, 1, 1);
 
 my ($packet) = makeheaders($src_host, $src_port, $dst_host, $dst_port);
 my ($destination) = pack('Sna4x8', AF_INET, $dst_port, $dst_host);
 send(RAW,$packet,0,$destination);
}

sub makeheaders {
 local($src_host,$src_port,$dst_host,$dst_port) = @_;
 my $zero_cksum = 0;
 # Lets construct the TCP half
 my $tcp_proto          = 6;
 my ($tcp_len)          = 20;
 my $syn                = 13456;
 my $ack                = 0;
 my $tcp_headerlen      = "5";
 my $tcp_reserved       = 0;
 my $tcp_head_reserved  = $tcp_headerlen .
                          $tcp_reserved;
 my $tcp_urg            = 0; # Flag bits
 my $tcp_ack            = 0; # eh no
 my $tcp_psh            = 0; # eh no
 my $tcp_rst            = 0; # eh no
 my $tcp_syn            = 1; # yeah lets make a connexion! :)
 my $tcp_fin            = 0;
 my $null               = 0;
 my $tcp_win            = 124;
 my $tcp_urg_ptr        = 0;
 my $tcp_all            = $null . $null .
                          $tcp_urg . $tcp_ack .
                          $tcp_psh . $tcp_rst .
                          $tcp_syn . $tcp_fin ;

 # In order to calculate the TCP checksum we have
 # to create a fake tcp header, hence why we did
 # all this stuff :) Stevens called it psuedo headers :)

 my ($tcp_pseudo) = pack('a4a4CCnnnNNH2B8nvn',
  $tcp_len,$src_port,$dst_port,$syn,$ack,
  $tcp_head_reserved,$tcp_all,$tcp_win,$null,$tcp_urg_ptr);

 my ($tcp_checksum) = &checksum($tcp_pseudo);

 # Now lets construct the IP packet
 my $ip_ver             = 4;
 my $ip_len             = 5;
 my $ip_ver_len         = $ip_ver . $ip_len;
 my $ip_tos             = 00;
 my ($ip_tot_len)       = $tcp_len + 20;
 my $ip_frag_id         = 19245;
 my $ip_frag_flag       = "010";
 my $ip_frag_oset       = "0000000000000";
 my $ip_fl_fr           = $ip_frag_flag . $ip_frag_oset;
 my $ip_ttl             = 30;

 # Lets pack this baby and ship it on out!
 my ($pkt) = pack('H2H2nnB16C2na4a4nnNNH2B8nvn',
  $ip_ver_len,$ip_tos,$ip_tot_len,$ip_frag_id,
  $ip_fl_fr,$ip_ttl,$tcp_proto,$zero_cksum,$src_host,
  $dst_host,$src_port,$dst_port,$syn,$ack,$tcp_head_reserved,
  $tcp_all,$tcp_win,$tcp_checksum,$tcp_urg_ptr);

 return $pkt;
}

sub checksum {
 # This of course is a blatent rip from _the_ GOD,
 # W. Richard Stevens.
  
 my ($msg) = @_;
 my ($len_msg,$num_short,$short,$chk);
 $len_msg = length($msg);
 $num_short = $len_msg / 2;
 $chk = 0;
 foreach $short (unpack("S$num_short", $msg)) {
  $chk += $short;
 }
 $chk += unpack("C", substr($msg, $len_msg - 1, 1)) if $len_msg % 2;
 $chk = ($chk >> 16) + ($chk & 0xffff);
 return(~(($chk >> 16) + $chk) & 0xffff);
}