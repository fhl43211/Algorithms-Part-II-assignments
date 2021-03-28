import java.awt.Color;
import edu.princeton.cs.algs4.Picture;

public class SeamCarver {
    private Picture _pic;
    private double[] _energies;


    // create a seam carver object based on the given picture
    public SeamCarver(Picture picture) {
        if (picture == null) {
            throw new IllegalArgumentException("null input");
        }
        _pic = new Picture(picture);
        _energies = new double[_pic.width()*_pic.height()];
        calculateAllEnergies(_pic, _energies);
    }

    private static void calculateAllEnergies(Picture pic, double[] energies) {
        for (int x = 0; x < pic.width(); ++x) {
            for (int y = 0; y < pic.height(); ++y) {
                calculateSingleEnergy(x, y, pic, energies);
            }
        }
    }

    private static void calculateSingleEnergy(int x, int y, Picture pic, double[] energies) {
        if (x == 0 || y == 0 || x == pic.width()-1 || y == pic.height()-1) {
            energies[calculateIndex(x,y, pic.width(), pic.height())] = 1000.;
        }
        else {
            Color xm1 = pic.get(x-1, y);
            Color xp1 = pic.get(x+1, y);
            Color ym1 = pic.get(x, y-1);
            Color yp1 = pic.get(x, y+1);
            double dxSq = (xm1.getRed() - xp1.getRed())*(xm1.getRed() - xp1.getRed()) +
                          (xm1.getGreen() - xp1.getGreen())*(xm1.getGreen() - xp1.getGreen()) +
                          (xm1.getBlue() - xp1.getBlue())*(xm1.getBlue() - xp1.getBlue());
            double dySq = (ym1.getRed() - yp1.getRed())*(ym1.getRed() - yp1.getRed()) +
                          (ym1.getGreen() - yp1.getGreen())*(ym1.getGreen() - yp1.getGreen()) +
                          (ym1.getBlue() - yp1.getBlue())*(ym1.getBlue() - yp1.getBlue());
            energies[calculateIndex(x,y, pic.width(), pic.height())] = Math.sqrt(dxSq + dySq);
        }
    }

    // current picture
    public Picture picture() {
        return new Picture(_pic);
    }

    // width of current picture
    public int width() {
        return _pic.width();
    }

    // height of current picture
    public int height() {
        return _pic.height();
    }

    private static void checkBound(int a, int bound) {
        if (a < 0 || a >= bound) {
            throw new IllegalArgumentException("Out of bounds");
        }
    }

    private static int calculateIndex(int x, int y, int w, int h) {
        checkBound(x, w);
        checkBound(y, h);
        return y*w + x;
    }

    // energy of pixel at column x and row y
    public double energy(int x, int y) {
        return _energies[calculateIndex(x, y, width(), height())];
    }

    // sequence of indices for horizontal seam
    public int[] findHorizontalSeam() {
        transpose();
        int[] seam = findVerticalSeam();
        transpose();
        return seam;
    }

    private void transpose() {
        int newWidth = _pic.height();
        int newHeight = _pic.width();
        int oldWidth = _pic.width();
        int oldHeight = _pic.height();
        Picture newPic = new Picture(newWidth, newHeight);
        double[] energies = new double[newWidth*newHeight];
        for (int newX = 0; newX < newWidth; ++newX) {
            for (int newY = 0; newY < newHeight; ++newY) {
                int oldX = newY;
                int oldY = newX;
                newPic.set(newX, newY, _pic.get(oldX, oldY));
                energies[calculateIndex(newX, newY, newWidth, newHeight)] = _energies[calculateIndex(oldX, oldY, oldWidth, oldHeight)];
            }
        }
        _pic = newPic;
        _energies = energies;
    }

    // sequence of indices for vertical seam
    public int[] findVerticalSeam() {
        int[][] cellTo = new int[width()][height()];
        double[][] pathEnergy = new double[width()][height()];
        for (int y = 0; y < height(); ++y) {
            for (int x = 0; x < width(); ++x) {
                pathEnergy[x][y] = Double.POSITIVE_INFINITY;
            }
        }
        int[] dxs = new int[3];
        dxs[0] = -1;
        dxs[1] = 0;
        dxs[2] = 1;

        int minX = 0;
        double minPathEnergy = Double.POSITIVE_INFINITY;
        for (int y = 0; y < height()-1; ++y) {
            for (int x = 0; x < width(); ++x) {
                if (y == 0) {
                    pathEnergy[x][y] = 1000.;
                    cellTo[x][y] = -1;
                }
                int nextY = y+1;
                for (int dx : dxs) {
                    int newX = x + dx;
                    if (newX >= 0 && newX < width()) {
                        double newPathEnergy = pathEnergy[x][y] + _energies[calculateIndex(newX, nextY, width(), height())];
                        if (newPathEnergy < pathEnergy[newX][nextY]) {
                            pathEnergy[newX][nextY] = newPathEnergy;
                            cellTo[newX][nextY] = x;
                        }
                    }
                }
                if (y == height()-2 && pathEnergy[x][y+1] < minPathEnergy) {
                    minPathEnergy = pathEnergy[x][y+1];
                    minX = x;
                }
            }
        }
        int[] seam = new int[height()];
        seam[height()-1] = minX;
        for (int y = height()-2; y >= 0; --y) {
            seam[y] = cellTo[seam[y+1]][y+1];
        }
        if (seam.length >= 2) {
            seam[0] = seam[1];
        }
        return seam;
    }

    // remove horizontal seam from current picture
    public void removeHorizontalSeam(int[] seam) {
        if (seam == null) {
            throw new IllegalArgumentException("Null input");
        }
        if (seam.length != _pic.width()) {
            throw new IllegalArgumentException("Out of bounds");
        }
        transpose();
        removeVerticalSeam(seam);
        transpose();
    }

    // remove vertical seam from current picture
    public void removeVerticalSeam(int[] seam) {
        if (seam == null) {
            throw new IllegalArgumentException("Null input");
        }
        if (seam.length != _pic.height()) {
            throw new IllegalArgumentException("Out of bounds");
        }
        int newWidth = _pic.width()-1;
        int newHeight = _pic.height();
        double[] newEnergy = new double[newWidth*newHeight];
        Picture newPic = new Picture(newWidth, newHeight);
        for (int y = 0; y < _pic.height(); ++y) {
            int skipX = seam[y];
            checkBound(skipX, _pic.width());
            if (y >= 1) {
                if (Math.abs(skipX - seam[y-1]) > 1) {
                    throw new IllegalArgumentException("bad seam");
                }
            }
            for (int x = 0; x < _pic.width(); ++x) {
                // x = [0, skipX)
                if (x < skipX) {
                    int newIndex = calculateIndex(x, y, newWidth, newHeight);
                    int oldIndex = calculateIndex(x, y, width(), height());
                    newEnergy[newIndex] = _energies[oldIndex];
                    newPic.set(x, y, _pic.get(x,y));
                }
                else if (x == skipX) continue;
                // x = [skipX+1, oldWidth);
                else {
                    int newIndex = calculateIndex(x-1, y, newWidth, newHeight);
                    int oldIndex = calculateIndex(x, y, width(), height());
                    newEnergy[newIndex] = _energies[oldIndex];
                    newPic.set(x-1, y, _pic.get(x,y));
                }
            }
        }
        for (int y = 0; y < _pic.height(); ++y) {
            int newX = seam[y]-1;
            if (newX >= 0 && newX < newWidth) {
                calculateSingleEnergy(newX, y, newPic, newEnergy);
            }
            newX = seam[y];
            if (newX >= 0 && newX < newWidth) {
                calculateSingleEnergy(newX, y, newPic, newEnergy);
            }
        }
        _pic = newPic;
        _energies = newEnergy;
    }

   //  unit testing (optional)
   public static void main(String[] args) {
       Picture pic = new Picture("seam/10x10.png");
       int[] seam = { 6, 8, 9, 9, 8, 7, 8, 7, 8, 8 };
       SeamCarver c = new SeamCarver(pic);
       c.removeVerticalSeam(seam);
   }

}