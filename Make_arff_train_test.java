import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class Make_arff_train_test {
	// 读写文件
	private static int dim = 256;
	private static String encoding = null;
	private static File file_input_100vectors =  new File("vectors_256TXTFull.bin");
	//private static File file_input_100vectors =  new File("vectors_100TXT_nopre.bin");
	private static File file_input_100w_data = new File("100W.utf8");
	private static File file_input_type80w = new File("message_type_80w");
	private static File file_out_arff_train_80w = new File("train_data_nopre_80w_" + dim + "_.arff");
	private static File file_out_arff_test_20w = new File("test_data_nopre_20w_" + dim + "_.arff");
	private static File file_output_linjie_train80 = new File("file_output_linjie_train80");//
	private static File file_output_linjie_test20 = new File("file_output_linjie_test20");//
	private static BufferedReader input_read_vectors = null;
	private static BufferedReader input_read_100w_data = null;
	private static BufferedReader input_read_type80w = null;
	private static BufferedWriter out_writer_train80 = null;
	private static BufferedWriter out_writer_test20 = null;
	private static BufferedWriter out_writer_train80_linjie = null;
	private static BufferedWriter out_writer_test20_linjie = null;

	public static void main(String[] args) {
		System.out.println("begin");
		init();
		System.out.println("init over.");
		process();
		System.out.println("process over");
		close();
	}

	private static void process() {
		Map<String, String> vectors_map = new HashMap<String, String>();
		try {
			out_writer_train80.write("@relation train\n");
			out_writer_test20.write("@relation test\n");
			for (int i = 0; i < dim; i++) {
				out_writer_train80.write("@attribute feature" + i + " real\n");
				out_writer_test20.write("@attribute feature" + i + " real\n");
			}
			out_writer_train80.write("@attribute class {1,0}\n\n");
			out_writer_train80.write("@data\n");
			out_writer_test20.write("@attribute class {1,0}\n\n");
			out_writer_test20.write("@data\n");

		} catch (IOException e1) {
			e1.printStackTrace();
		}

		try {
			String line = input_read_vectors.readLine();// Word2vec结果的第一行不要
			while ((line = input_read_vectors.readLine()) != null) {// line的后面也就是map的值中是没有"\n"的
				line = line.replace("\n", "").trim();
				int index_space = line.indexOf(" ");
				vectors_map.put(line.substring(0, index_space), line.substring(index_space + 1).replaceAll(" ", ","));
			} // word2vec的所有词放到了map中

			int step = 0;
			while ((line = input_read_100w_data.readLine()) != null) {
				try {
					double[] sum_vector = new double[dim];
					for (int i = 0; i < dim; i++) {
						sum_vector[i] = 0.0;
					}
					double[] avg_vector = new double[dim];
					for (int i = 0; i < dim; i++) {
						avg_vector[i] = 0.0;
					}

					String[] line_list = line.split(" ");
					for (String word : line_list) {
						if (vectors_map.get(word) == null) {
							continue;
						}
						String temp_vector = vectors_map.get(word);
						String[] vec_list = temp_vector.split(",");// dim个数
						// String 类型
						double[] vec_list_double = new double[dim];
						for (int i = 0; i < dim; i++) {
							vec_list_double[i] = Double.parseDouble(vec_list[i]);
							sum_vector[i] += vec_list_double[i];
						}
					} // sum_vec is ok
					String type;
					type = input_read_type80w.readLine();
					step++;
					if(step % 50000 == 0){
						System.out.println(step);
					}
						
					if (step <= 800000) {
						for (int i = 0; i < dim; i++) {
							avg_vector[i] = sum_vector[i] / dim;
							out_writer_train80.write(avg_vector[i] + ",");
							out_writer_train80_linjie.write(avg_vector[i] + ",");
						}
						out_writer_train80_linjie.write(type + "\n");
						out_writer_train80.write(type + "\n");//
						if(step % 5000 == 0){
							System.out.println(step);
						}
					} else if (step <= 1000000) {
						for (int i = 0; i < dim; i++) {
							avg_vector[i] = sum_vector[i] / dim;
							out_writer_test20.write(avg_vector[i] + ",");
							out_writer_test20_linjie.write(avg_vector[i] + ",");
						}
						if(step % 1000 == 0){
							System.out.println(step);
						}
						
						out_writer_test20_linjie.write("1" + "\n");
						out_writer_test20.write("1" + "\n");
					} else {
						break;
					}
				} catch (Exception e) {
					continue;
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void init() {
		encoding = "utf-8";

		try {
			input_read_vectors = new BufferedReader(
					new InputStreamReader(new FileInputStream(file_input_100vectors), encoding));
			input_read_100w_data = new BufferedReader(
					new InputStreamReader(new FileInputStream(file_input_100w_data), encoding));
			input_read_type80w = new BufferedReader(
					new InputStreamReader(new FileInputStream(file_input_type80w), encoding));
		} catch (UnsupportedEncodingException | FileNotFoundException e) {
			e.printStackTrace();
		}
		try {
			out_writer_train80 = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(file_out_arff_train_80w, false), encoding));
			out_writer_test20 = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(file_out_arff_test_20w, false), encoding));
			out_writer_train80_linjie = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(file_output_linjie_train80, false), encoding));
			out_writer_test20_linjie = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(file_output_linjie_test20, false), encoding));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	private static void close() {
		try {
			input_read_vectors.close();
			input_read_100w_data.close();
			input_read_type80w.close();
			out_writer_test20.close();
			out_writer_train80.close();
			out_writer_test20_linjie.close();
			out_writer_train80_linjie.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
