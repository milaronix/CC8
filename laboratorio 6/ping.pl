#!/usr/local/bin/perl

use Socket;

$src_host = $ARGV[0]; # The source IP/Hostname
$src_port = $ARGV[1]; # The Source Port
$dst_host = $ARGV[2]; # The Destination IP/Hostname
$dst_port = $ARGV[3]; # The Destination Port.

if(!defined $src_host or !defined $src_port or !defined $dst_host or !defined $dst_port) {
    print "Usage: $0 <source host> <source port> <dest host> <dest port>\n";
    exit;
 }else{
    main();
}


sub main {
    socket(SOCKET, AF_INET, SOCK_RAW, 255) || die $!;
    setsockopt(SOCKET, 0, 1, 1);

    #ojo, las variables $srcHost, $srcPort, $dstHost, $dstPort no estan definidas, ustedes las tienen que leer de la linea de comandos
    my $packet = headers($srcHost, $srcPort, $dstHost, $dstPort);
    my $destination = pack('Sna4x8', AF_INET, $dstPort, $dstHost);
    send(SOCKET,$packet,0,$destination);
}


sub headers {
 local($srcHost,$srcPort,$dstHost,$dstPort) = @_;
 #aqui tienen que hacer su magia
}

#para el calculo del checksum podrian usar una funcion como la siguiente:
sub checksum {
    my $msg = shift;
    my $length = length($msg);
    my $numShorts = $length/2;
    my $sum = 0;

    foreach (unpack("n$numShorts", $msg)) {
       $sum += $_;
    }

    $sum += unpack("C", substr($msg, $length - 1, 1)) if $length % 2;
    $sum = ($sum >> 16) + ($sum & 0xffff);
    return(~(($sum >> 16) + $sum) & 0xffff);
}
