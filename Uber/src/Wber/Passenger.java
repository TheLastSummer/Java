package Wber;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 乘客类
 */
public class Passenger implements Runnable{
    private static AtomicInteger passengerCount = new AtomicInteger();
    private int num;/*乘客编号*/
    private Location startLocation;/*初始坐标*/
    private Location destinationLocation;/*目的地坐标*/
    private Set<Car> cars;/*出租车队列*/
    private HashSet<Car> fitCars = new HashSet<>();/*满足条件的出租车队列*/
    private boolean accept = false;/*是否接受*/
    private Center center;

    /**
     * 构造器
     */
    public Passenger(Center center, Set<Car> cars) {
        passengerCount.addAndGet(1);
        num = passengerCount.get();
        startLocation = new Location(new Random().nextInt(80), new Random().nextInt(80));
        destinationLocation = new Location(new Random().nextInt(80), new Random().nextInt(80));
        this.cars = cars;
        this.center = center;
    }

    public int getNum() {
        return num;
    }

    public Location getStartLocation() {
        return startLocation;
    }

    public Location getDestinationLocation() {
        return destinationLocation;
    }

    /**
     * 遍历出租车队列寻找满足条件的出租车
     * @return 找到满足条件的出租车，返回真
     */
    public boolean traverseCars(){
        synchronized (cars) {
            Iterator<Car> carIterator = cars.iterator();
            while (carIterator.hasNext()) {
                Car car = carIterator.next();
                if (inPassengerRange(startLocation, car.getLocation())) {
                    fitCars.add(car);
                }
            }
        }
        return !fitCars.isEmpty();
    }

    /**
     * 根据信用度寻找最合适的出租车
     * @return 最终选择的出租车
     */
    public Car findBestFitCar(){
//        synchronized (fitCars) {
        Iterator<Car> carIterator = fitCars.iterator();
        Car chosenCar = carIterator.next();
        int chosenCarPathSize = chosenCar.findPath(chosenCar.getLocation(), startLocation).size();
        while (carIterator.hasNext()) {
            Car car = carIterator.next();
            int carPathSize = car.findPath(car.getLocation(), startLocation).size();
            if(car.getCredit() > chosenCar.getCredit()){
                chosenCar = car;
                chosenCarPathSize = carPathSize;
            }
            else if(car.getCredit() == chosenCar.getCredit()
                    && carPathSize <= chosenCarPathSize
                    && car.getCarState() == CarState.Waiting){
                chosenCar = car;
                chosenCarPathSize = carPathSize;
            }
        }
        if(chosenCar.getCarState() != CarState.Waiting)
            return null;
        return chosenCar;
//        }
    }

    /**
     * 判断出租车是否在乘客请求范围内
     * @param passengerLocation 乘客坐标
     * @param carLocation 出租车坐标
     * @return 如果在请求范围内，返回true
     */
    public boolean inPassengerRange(Location passengerLocation, Location carLocation) {
        return (carLocation.getX() >= passengerLocation.getX() - 2 || carLocation.getX() <= passengerLocation.getX() + 2)
                && (carLocation.getY() >= passengerLocation.getY() - 2 || carLocation.getY() <= passengerLocation.getY() + 2);
    }

    @Override
    public void run(){
        long startTime = System.currentTimeMillis();
        try {
            while (true) {
                long endTime = System.currentTimeMillis();
                if (endTime - startTime > 3000)
                    break;/*时间到达三秒，线程结束*/
                else {
                    traverseCars();
                    Thread.sleep(100);
                }
            }
            synchronized (this.getClass()) {
                if (fitCars.isEmpty())
                    System.out.println(num + " 号乘客无可用乘客");
                else {
                    Car car = findBestFitCar();
                    if (car != null) {
//                    center.setChosenCars(new AskedCar(this, car));
                        car.setCarState(CarState.WaitServing);
                        car.setStartLocation(this.startLocation);
                        car.setDestinationLocation(this.destinationLocation);
                        System.out.println(num + " 号乘客上了 " + car.getNum() + " 号车");
                    }
                    else
                        System.out.println(num + " 号乘客无可用车");
                }
            }
        }
        catch (InterruptedException i){
            i.printStackTrace();
        }
    }
}