require 'date'
require 'bigdecimal'
require 'bigdecimal/util'

start_time = DateTime.now
puts start_time
baserate = BigDecimal("0.0013");   # low call rate
distrate = BigDecimal("0.00894");  # high call rate
basetax  = BigDecimal("0.0675");   # base tax rate
disttax = BigDecimal("0.0341");   # distance tax rate
ZERO = BigDecimal("0");        # zero
DECIMAL_HUNDRED = BigDecimal("100.0")

sumP = ZERO   # sum of prices
sumB = ZERO   # sum of basic tax
sumD = ZERO   # sum of 'distance' tax

txt = File.open('c:\tmp\telco_ruby.txt', 'w')
f = open('expon180.1e6')
#f = open('telco.test')
txt.puts '  Time Rate | Price   Btax   Dtax  | Output'
txt.puts '------------+----------------------+--------'
while bytes = f.read(8)
  dec = bytes.unpack('H*').first.to_i
  tipo = bytes.unpack('B*').to_s[61, 1]
  if dec == 0
    next
  end
  n = BigDecimal(dec)

  if tipo == '1'
    ori = 'D'
    price = distrate * n
  else
    ori = 'L'
    price = baserate * n
  end
  price = price.round(2, half: :even)
  sumP = sumP + price

  b = price * basetax
  b = b.round(2, :down)
  sumB = sumB + b

  d = ZERO
  if tipo == '1'
    d = price * disttax
    d = d.round(2, :down)
    sumD = sumD + d
  end
  t = price + b + d 

  txt.puts sprintf(" %5.0f   %s  | %5.2f  %5.2f  %5.2f  | %5.2f", n, ori, price, b, d, t) 
end
txt.puts '------------+----------------------+--------'
txt.puts sprintf("   Totals:  | %5.2f  %5.2f  %5.2f  | %5.2f", sumP, sumB, sumD, (sumP + sumB + sumD)) 

txt.close
f.close
end_time = DateTime.now
puts end_time
elapsed_milis = ((end_time - start_time) * 24 * 60 * 60 * 1000).to_i
puts "Elapsed = #{elapsed_milis} miliseconds"


# ? time ruby -e "File.open('large.txt','r').each { |line| line }"

# real  0m1.410s
# user  0m1.231s
# sys   0m0.089s